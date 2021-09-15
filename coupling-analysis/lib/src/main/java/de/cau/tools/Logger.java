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

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.cau.config.Configuration;
import de.cau.config.DirectoryTool;
import de.cau.monitor.metrics.ACoupling;

/**
 * This is a very simple logger class, that stores the logs in a file and can
 * print to the console.
 *
 */
public class Logger {

	private FileWriter writer;
	private boolean isConsolePrinting = true;
	public String fileName;
	private static final String SEPARATION = "-------------------------------";

	private static Logger loggerInstance;

	/**
	 * Getting singleton instance.
	 * 
	 * @return
	 */
	public static Logger getInstance() {
		if (loggerInstance == null) {
			loggerInstance = new Logger("meta");
			loggerInstance.isConsolePrinting(Configuration.isConsolePrinting);
		}
		return loggerInstance;
	}

	/**
	 * Creates new logger and forces to flush.
	 */
	public void updateChanges() {
		try {
			writer.flush();
			writer.close();
			this.writer = new FileWriter(DirectoryTool.getOutputRoot() + fileName + ".log", false);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private Logger(final String fileName) {
		this.fileName = fileName;
		try {
			this.writer = new FileWriter(DirectoryTool.getOutputRoot() + fileName + ".log", false);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Log a list of Strings.
	 * 
	 * @param strs
	 */
	public void logAll(final List<String> strs) {
		for (final String str : strs) {
			this.log(str);
		}
	}

	/**
	 * Logs the Objects string representation.
	 * 
	 * @param obj
	 */
	public void log(final Object obj) {
		log(obj.toString());
	}

	/**
	 * Logs the message.
	 * 
	 * @param message
	 */
	public void log(final String message) {

		if (isConsolePrinting) {
			System.out.println(message);
		}

		try {
			this.writer.write(message + "\n");

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes the logger.
	 */
	public void close() {
		try {
			this.writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Enable or disable console printing.
	 * 
	 * @param flag
	 */
	public void isConsolePrinting(final boolean flag) {
		this.isConsolePrinting = flag;
	}

	/**
	 * Wrapes tag in braces.
	 * 
	 * @param tag
	 * @return
	 */
	private String wrapWithBraces(final Tag tag) {
		return "[" + tag + "]";
	}

	/**
	 * Log by given tag.
	 * 
	 * @param msg      message
	 * @param tag
	 * @param metaInfo
	 * @param coupl
	 */
	public void logByTag(final String msg, final Tag tag, final MetaInfo metaInfo, final ACoupling coupl) {

		switch (tag) {
		case RESOLVED: {
			logResolved(msg);
			metaInfo.countAsResolved(coupl);
			break;
		}
		case NOT_RESOLVED: {
			logNotResolved(msg);
			metaInfo.countAsError(coupl, msg);
			break;
		}
		case ERROR: {
			logError(msg);
			break;
		}
		case UNSUPPORTED: {
			logUnsupported(msg);
			metaInfo.countAsError(coupl, msg);
			break;
		}
		case WARNING: {
			logWarning(msg);
			break;
		}
		case SUCCESFULLY_PARSED: {
			logSuccesfullParsed(msg);
			break;
		}
		case PARSE_ERROR: {
			logParseError(msg);
			break;
		}
		case NOT_PROJECT_PART: {
			logResolved(msg);
			metaInfo.countAsNotPart(coupl);
			break;
		}
		case IGNORED: {
			logIgnored(msg);
			metaInfo.countAsNotPart(coupl);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + tag);
		}
	}

	public void logResolved(final String message) {
		loggerInstance.log(wrapWithBraces(Tag.RESOLVED) + ": " + message);
	}

	public void logNotResolved(final String message) {
		loggerInstance.log(wrapWithBraces(Tag.NOT_RESOLVED) + ": " + message);
	}

	public void logError(final String message) {
		loggerInstance.log(wrapWithBraces(Tag.ERROR) + ": " + message);
	}

	public void logUnsupported(final String message) {
		loggerInstance.log(wrapWithBraces(Tag.UNSUPPORTED) + ": " + message);
	}

	public void logWarning(final String message) {
		loggerInstance.log(wrapWithBraces(Tag.WARNING) + ": " + message);
	}

	public void logSuccesfullParsed(final String message) {
		loggerInstance.log(wrapWithBraces(Tag.SUCCESFULLY_PARSED) + ": " + message);
	}

	public void logParseError(final String message) {
		loggerInstance.log(wrapWithBraces(Tag.PARSE_ERROR) + ": " + message);
	}

	public void logFilteredOut(final String message) {
		loggerInstance.log(wrapWithBraces(Tag.FILTERED_OUT) + ": " + message);
	}

	public void logIgnored(final String message) {
		loggerInstance.log(wrapWithBraces(Tag.IGNORED) + ": " + message);
	}

	public void logSeparation() {
		loggerInstance.log(SEPARATION);
	}
}
