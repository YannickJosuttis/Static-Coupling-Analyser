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

package de.cau.sourcecode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import de.cau.config.DirectoryTool;
import de.cau.tools.FunctionHelper;
import de.cau.tools.Logger;

public class ASTParser {

	Logger logger;

	public ASTParser() {
		this.logger = Logger.getInstance();
	}

	/**
	 * Get all {@link CompilationUnit}s of predefined sourcecode root. Within this
	 * function also type solvers are configured based on predefined configuration,
	 * this is needed to solve symbols.
	 * 
	 * @return list of {@link CompilationUnit} units, the list could contain empty
	 *         units , if the could not be parse correctly.
	 */
	public List<Optional<CompilationUnit>> getAllCompilationUnits() {

		logger.log("...Collecting source files of given root: " + DirectoryTool.getSourceCodeInputRoot());
		// Get the java files of given root.
		final String[] patterns = { "**/*.java" };
		final List<String> sourceList = Arrays
				.asList(DirectoryTool.filesScannedInDirectory(DirectoryTool.getSourceCodeInputRoot(), patterns));

		logger.logAll(sourceList);
		logger.logSeparation();

		setupSymbolSolver();

		logger.logSeparation();
		logger.log("Parsing java files to Abstract Syntax Tree");
		logger.logSeparation();

		// Parse all files.
		final List<Optional<CompilationUnit>> cus = sourceList
				.stream()
				.map(FunctionHelper
						.handleExceptionFunctionWithWrapper(source -> StaticJavaParser.parse(new File(source))))
				.collect(Collectors.toList());
		logger.logSeparation();

		return cus;
	}

	/**
	 * Setup the symbol solvers like {@link JarTypeSolver},
	 * {@link ReflectionTypeSolver} and {@link JavaParserTypeSolver}.
	 */
	private void setupSymbolSolver() {

		final List<TypeSolver> parseTypeSolvers = new ArrayList<>();
		parseTypeSolvers.add(new JavaParserTypeSolver(DirectoryTool.getSourceCodeInputRoot()));

		// Add the ReflectionTypeSolver for resolve internal connections of java
		// in-build-functions.
		parseTypeSolvers.add(new ReflectionTypeSolver());

		// Add external Library files.
		DirectoryTool.getExternalLibPath().ifPresent(libDir -> {

			final List<String> libSources = Arrays
					.asList(DirectoryTool.filesScannedInDirectory(libDir, new String[] { "**/*.jar" }));

			libSources
					.stream()
					// Create and collect JarType Solvers form given libraries and map to the
					// corresponding library name
					.map(FunctionHelper.handleExceptionFunction(lib -> {
						parseTypeSolvers.add(new JarTypeSolver(lib));
						return lib.substring(lib.lastIndexOf("/") + 1, lib.length());
					}))
					.collect(Collectors.toList());

			logger.log("External libraries found:");
			logger.logSeparation();
			logger.logAll(libSources);

		});

		// Setup java symbol solver
		final TypeSolver typeSolver = new CombinedTypeSolver(parseTypeSolvers);
		final JavaSymbolSolver symSolv = new JavaSymbolSolver(typeSolver);
		StaticJavaParser.getConfiguration().setSymbolResolver(symSolv);
	}
}
