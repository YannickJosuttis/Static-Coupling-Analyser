/*
 * Copyright [2021] [Hannah S. Fischer und Yannick Josuttis]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.cau.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.cau.monitor.metrics.CouplingTag;

/**
 * This annotation is useful for development and testing purpose by simply
 * setting basic parameter for the analyzer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Config {

	public boolean isConsolePrinting() default true;

	public boolean isMeasuringSourceCode() default true;

	public boolean isMeasuringByteCode() default false;

	public CouplingTag[] sourceCodeMetrics() default {};

	public CouplingTag[] byteCodeMetrics() default {};

	public boolean countSelfConnection() default false;

	public String byteCodeInputRoot() default "./bin/main";

	public String sourceCodeInputRoot() default "./src/main/java";

	public String outputRoot() default "./";

	public String externalLibPath() default Configuration.UNDIFIND;

}
