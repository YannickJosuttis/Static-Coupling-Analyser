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

package de.cau.tools;

/**
 * Useful tags for analyzing purpose.
 */
public enum Tag {
	RESOLVED, NOT_RESOLVED, ERROR, UNSUPPORTED, WARNING, SUCCESFULLY_PARSED, PARSE_ERROR, NOT_PROJECT_PART,
	FILTERED_OUT, IGNORED, CACHE, LAMBDA
}
