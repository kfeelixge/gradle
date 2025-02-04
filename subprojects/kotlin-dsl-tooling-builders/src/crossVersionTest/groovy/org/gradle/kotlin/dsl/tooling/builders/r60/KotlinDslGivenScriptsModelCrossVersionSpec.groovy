/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.kotlin.dsl.tooling.builders.r60

import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.test.fixtures.file.LeaksFileHandles
import spock.lang.Ignore

@TargetGradleVersion(">=6.0")
@LeaksFileHandles("Kotlin Compiler Daemon taking time to shut down")
@Ignore("https://github.com/gradle/gradle-private/issues/3500")
class KotlinDslGivenScriptsModelCrossVersionSpec extends AbstractKotlinDslScriptsModelCrossVersionSpec {

    def "can fetch model for a given set of scripts"() {

        given:
        def spec = withMultiProjectBuildWithBuildSrc()
        def requestedScripts = spec.scripts.values() + spec.appliedScripts.some

        when:
        def model = kotlinDslScriptsModelFor(requestedScripts)

        then:
        model.scriptModels.keySet() == requestedScripts as Set

        and:
        assertModelMatchesBuildSpec(model, spec)

        and:
        assertModelAppliedScripts(model, spec)
    }

    def "can fetch model for a given set of scripts of a build in lenient mode"() {

        given:
        def spec = withMultiProjectBuildWithBuildSrc()
        def requestedScripts = spec.scripts.values() + spec.appliedScripts.some

        and:
        spec.scripts.a << """
            script_body_compilation_error
        """

        when:
        def model = kotlinDslScriptsModelFor(true, requestedScripts)

        then:
        model.scriptModels.keySet() == requestedScripts as Set

        and:
        assertModelMatchesBuildSpec(model, spec)

        and:
        assertModelAppliedScripts(model, spec)

        and:
        assertHasExceptionMessage(
            model,
            spec.scripts.a,
            "Unresolved reference: script_body_compilation_error"
        )
    }
}
