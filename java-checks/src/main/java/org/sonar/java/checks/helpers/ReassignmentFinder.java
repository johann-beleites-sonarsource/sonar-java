/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.checks.helpers;

import org.sonar.java.syntaxtoken.FirstSyntaxTokenFinder;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.Tree;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ReassignmentFinder extends BaseTreeVisitor {

  private final List<IdentifierTree> usages;
  private List<Tree> reassignments;

  private ReassignmentFinder(List<IdentifierTree> usages) {
    this.usages = usages;
    this.reassignments = new LinkedList<>();
  }

  @CheckForNull
  public static Tree getReassignmentOrDeclaration(Tree startingPoint, Symbol referenceSymbol) {
    Tree result = referenceSymbol.declaration();
    List<IdentifierTree> usages = referenceSymbol.usages();
    if (usages.size() == 1) {
      return result;
    }
    
    List<Tree> reassignments = getReassignments(referenceSymbol.owner().declaration(), usages);

    int line = FirstSyntaxTokenFinder.firstSyntaxToken(startingPoint).line();
    Tree lastReassignment = getLastReassignment(line, reassignments);
    if (lastReassignment != null) {
      return lastReassignment;
    }
    return result;
  }

  private static List<Tree> getReassignments(@Nullable Tree ownerDeclaration, List<IdentifierTree> usages) {
    if (ownerDeclaration != null) {
      ReassignmentFinder reassignmentFinder = new ReassignmentFinder(usages);
      ownerDeclaration.accept(reassignmentFinder);
      return reassignmentFinder.reassignments;
    }
    return new ArrayList<>();
  }

  @CheckForNull
  private static Tree getLastReassignment(int line, List<Tree> reassignments) {
    Tree result = null;
    for (Tree reassignment : reassignments) {
      int reassignmentLine = FirstSyntaxTokenFinder.firstSyntaxToken(reassignment).line();
      if (line > reassignmentLine) {
        result = reassignment;
      }
    }
    return result;
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if (isSearchedVariable(tree.variable())) {
      reassignments.add(tree);
    }
    super.visitAssignmentExpression(tree);
  }

  private boolean isSearchedVariable(ExpressionTree variable) {
    return variable.is(Tree.Kind.IDENTIFIER) && usages.contains(variable);
  }
}
