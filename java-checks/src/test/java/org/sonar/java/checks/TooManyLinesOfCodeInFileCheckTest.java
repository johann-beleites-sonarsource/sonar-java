/*
 * SonarQube Java
 * Copyright (C) 2012-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

import static org.assertj.core.api.Assertions.assertThat;

import static org.sonar.java.CheckTestUtils.testSourcesPath;

class TooManyLinesOfCodeInFileCheckTest {

  @Test
  void testDefault() {
    assertThat(new TooManyLinesOfCodeInFileCheck().maximum).isEqualTo(750);
  }

  @Test
  void test() {
    TooManyLinesOfCodeInFileCheck check = new TooManyLinesOfCodeInFileCheck();
    check.maximum = 1;
    JavaCheckVerifier.newVerifier()
      .onFile(testSourcesPath("checks/TooManyLinesOfCode.java"))
      .withCheck(check)
      .verifyIssueOnFile("This file has 11 lines, which is greater than 1 authorized. Split it into smaller files.");
  }

  @Test
  void test2() {
    JavaCheckVerifier.newVerifier()
      .onFile(testSourcesPath("checks/TooManyLinesOfCode.java"))
      .withCheck(new TooManyLinesOfCodeInFileCheck())
      .verifyNoIssues();
  }

}
