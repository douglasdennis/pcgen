/*
 * Copyright 2012 Vincent Lhote
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package translation.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateFormatUtils;

/**
 * This class allows the generation of a PO Template file from the tips.txt files,
 * and also generate translated tips ({@code tips_XX.txt}) from a PO file (whose name is {@code _XX.po}).
 * PO Template and PO files are message catalog used in gettext.
 * The duplicates from the tips files should appear only once in the PO Template files.
 * 
 * This class tries to be independent of code, but still needs Apache Commons Lang.
 * 
 * @author Vincent Lhote
 * @see <a href="http://www.gnu.org/software/gettext/manual/gettext.html">GNU gettext manual</a>
 */
public class Tips
{
	/** Quote char */
	private static final char QUOTE = '"';

	/**
	 * 
	 */
	private static final String COMMENT_PREFIX = "#"; //$NON-NLS-1$

	private static final String DEFAULT_TIPS_FILENAME = "tips.txt"; //$NON-NLS-1$

	/** true to add a message to tips that are not translated, false to copy them as is so they won’t appear */
	private static final boolean MARK_UNTRANSLATED = true;

	public static void generatePOT(File rootDirectory, String potFilename)
	{
		generatePOT(rootDirectory, potFilename, DEFAULT_TIPS_FILENAME);
	}

	/**
	 * 
	 * @param rootDirectory root of the directories to parse
	 * @param filename the name of the filename to parse
	 */
	public static void generatePOT(File rootDirectory, String potFilename, String filename)
	{
		Set<String> tips = new HashSet<String>();
		// search for each filename in the sub directory of rootDirectory
		if (rootDirectory.isDirectory())
		{
			FilenameFilter filter = new SpecificFilenameFilter(filename);
			File[] subfiles = rootDirectory.listFiles();
			for (int i = 0; i < subfiles.length; i++)
			{
				if (subfiles[i].isDirectory())
				{
					File[] tipsFiles = subfiles[i].listFiles(filter);
					for (int j = 0; j < tipsFiles.length; j++)
					{
						log("Found {0}", tipsFiles[j]);
						// for each non comment line of the file, put its content in a Set<String>
						try
						{
							BufferedReader reader = new BufferedReader(new FileReader(tipsFiles[j]));
							addTips(tips, reader);
							reader.close();
						}
						catch (FileNotFoundException e)
						{
							logError("Warning: file found then not found {0}, ignoring this file", tipsFiles[j]);
							e.printStackTrace();
						}
						catch (IOException e)
						{
							logError("Warning: IO error reading {0}, ignoring this file", tipsFiles[j]);
							e.printStackTrace();
						}

					}
				}
			}

		}

		// generate the pot
		writePOT(tips, potFilename);
		log("Done");
	}

	/**
	 * @param tips
	 * @param potFilename
	 */
	private static void writePOT(Set<String> tips, String potFilename)
	{
		File pot = new File(potFilename);
		// create parent if necessary
		pot.getParentFile().mkdirs();

		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(pot));
			writePOT(tips, bw);
			log("Wrote {0}", potFilename);
		}
		catch (IOException e)
		{
			logError("IO error while writing {0}", pot);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (bw != null)
					bw.close();
			}
			catch (IOException e)
			{
				logError("IO error while closing {0}", pot);
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param tips
	 * @param bw
	 * @throws IOException 
	 */
	private static void writePOT(Set<String> tips, BufferedWriter bw) throws IOException
	{
		// header stuff
		Calendar now = Calendar.getInstance();
		bw.write("msgid \"\"\n" + "msgstr \"\"\n" + "\"Project-Id-Version: PCGen-tips 6.x/SVN?\\n\"\n"
			+ "\"Report-Msgid-Bugs-To: \\n\"\n" + "\"POT-Creation-Date: "
			+ DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(now) + "\\n\"\n"
			+ "\"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\\n\"\n"
			+ "\"Last-Translator: FULL NAME <EMAIL@ADDRESS>\\n\"\n" + "\"Language-Team: LANGUAGE <LL@li.org>\\n\"\n"
			+ "\"MIME-Version: 1.0\\n\"\n" + "\"Content-Type: text/plain; charset=UTF-8\\n\"\n"
			+ "\"Content-Transfer-Encoding: 8bit\\n\"\n\n");

		// filecontent
		MessageFormat msgid = new MessageFormat("msgid \"{0}\""); //$NON-NLS-1$
		String msgstr = "msgstr \"\""; //$NON-NLS-1$
		for (String tip : tips)
		{
			bw.write(msgid.format(new Object[]{escape(tip)}));
			bw.write("\n");
			bw.write(msgstr);
			bw.write("\n\n");
		}
	}

	protected static void addTips(Set<String> tips, BufferedReader reader)
	{
		String line;
		try
		{
			line = reader.readLine();
			while (line != null)
			{
				if (isTip(line))
					addTip(tips, line);
				line = reader.readLine();
			}
		}
		catch (IOException e)
		{
			logError("Warning: IO error reading a line, ignoring it");
			e.printStackTrace();
		}
	}

	protected static boolean isTip(String line)
	{
		return line != null && !line.isEmpty() && !line.startsWith(COMMENT_PREFIX);
	}

	protected static void addTip(Set<String> tips, String tip)
	{
		tips.add(tip);
	}

	/**
	 * Filter on a specific filename.
	 */
	static class SpecificFilenameFilter implements FilenameFilter
	{

		private final String filename;

		/**
		 * @param filename
		 */
		public SpecificFilenameFilter(String filename)
		{
			this.filename = filename;
		}

		@Override
		public boolean accept(File dir, String name)
		{
			return filename.equals(name);
		}

	}

	public static void generateTips(File rootDirectory, File translation, String translationName)
	{
		generateTips(rootDirectory, translation, translationName, DEFAULT_TIPS_FILENAME);
	}

	/**
	 * 
	 * @param rootDirectory directory to search in (only done in sub-directories of this)
	 * @param translation PO file
	 * @param translationName name for new translation filename (like tips_fr.txt)
	 * @param originalName original filename (like tips.txt)
	 */
	public static void generateTips(File rootDirectory, File translation, String translationName, String originalName)
	{
		int statUntranslated = 0, statTranslated = 0;
		// load stuff from the PO catalog file
		Map<String, String> tipsTranslated = new HashMap<String, String>();
		BufferedReader translationReader = null;
		try
		{
			translationReader = new BufferedReader(new FileReader(translation));
			String line = translationReader.readLine();
			String key = null;
			StringBuilder str = new StringBuilder();
			while (line != null)
			{
				if (line.startsWith("msgid"))
				{
					if (key != null)
					{
						// add previous appended translation with the id in the map
						tipsTranslated.put(key, str.toString());
						if (str.toString().isEmpty())
							statUntranslated++;
						else statTranslated++;
						str = new StringBuilder();
					}
					key = line.substring(line.indexOf(QUOTE) + 1, line.lastIndexOf(QUOTE));
				}
				// TODO remove escape quote?
				if (line.startsWith("msgstr") || line.startsWith("\""))
				{
					String substring = line.substring(line.indexOf(QUOTE) + 1, line.lastIndexOf(QUOTE));
					substring = removeEscaped(substring);
					str.append(substring);
				}
				line = translationReader.readLine();
			}
			// adds last key/translation
			if (key != null)
			{
				tipsTranslated.put(key, str.toString());
				if (str.toString().isEmpty())
					statUntranslated++;
				else statTranslated++;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (translationReader != null)
				try
				{
					translationReader.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		log("Translated tips: {0}", statTranslated);
		log("Untranslated tips: {0}", statUntranslated);

		// find the originalName file in each subdir of root
		if (rootDirectory.isDirectory())
		{
			FilenameFilter filter = new SpecificFilenameFilter(originalName);
			File[] subfiles = rootDirectory.listFiles();
			for (int i = 0; i < subfiles.length; i++)
			{
				if (subfiles[i].isDirectory())
				{
					File[] tipsFiles = subfiles[i].listFiles(filter);
					for (int j = 0; j < tipsFiles.length; j++)
					{
						File newFile = new File(subfiles[i], translationName);
						log("Found {0}, creating {1}", tipsFiles[j], newFile);
						BufferedWriter bw = null;
						BufferedReader reader = null;
						try
						{
							reader = new BufferedReader(new FileReader(tipsFiles[j]));
							bw = new BufferedWriter(new FileWriter(newFile));
							String readLine = reader.readLine();
							while (readLine != null)
							{
								if (isTip(readLine))
								{
									String translatedLine = tipsTranslated.get(readLine);
									if (translatedLine == null)
									{
										log("null translated line in {1}, original {0}", readLine, translation);
										translatedLine = readLine;
									}
									else if (translatedLine.isEmpty() && MARK_UNTRANSLATED)
									{
										translatedLine = "<em>Not yet translated</em><br>" + readLine;
									}
									bw.write(translatedLine);
								}
								else bw.write(readLine);
								bw.write("\n");
								readLine = reader.readLine();
							}
						}
						catch (FileNotFoundException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						finally
						{
							try
							{
								if (reader != null)
									reader.close();
							}
							catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try
							{
								if (bw != null)
									bw.close();
							}
							catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}
				}
			}

		}
		log("Done");

	}

	/* Escape related methods */

	/**
	 * Handle string escapes in the PO files.
	 * @param string
	 * @return non escaped string
	 */
	@SuppressWarnings("nls")
	protected static String removeEscaped(String string)
	{
		return string.replaceAll("\\\\\'", "'").replaceAll("\\\\\"", "\"").replaceAll("\\\\\\\\", "\\\\");
	}

	/**
	 * @param tip
	 * @return
	 */
	@SuppressWarnings("nls")
	protected static String escape(String string)
	{
		return string.replaceAll("\\\\", "\\\\\\\\").replaceAll("\'", "\\\\\'").replaceAll("\"", "\\\\\"");
	}

	/* main and related methods */

	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			logError("Missing argument");
			usage();
			return;
		}
		// TODO detect what to do based on arguments
		if ("POT".equals(args[0]))
		{
			// TODO handle missing argument case
			if (args.length == 4)
				generatePOT(new File(args[1]), args[2], args[3]);
			else generatePOT(new File(args[1]), args[2]);
		}
		else if ("tips".equals(args[0]))
		{
			// TODO handle missing argument case
			if (args.length == 5)
				generateTips(new File(args[1]), new File(args[2]), args[3], args[4]);
			else generateTips(new File(args[1]), new File(args[2]), args[3]);
		}
		else
		{
			logError("Unknown command");
			usage();
		}
	}

	/**
	 * 
	 */
	private static void usage()
	{
		log("Usage:");
		log("\t<command> POT rootDirectory potFilename [tipsFilename]");
		log("\t<command> tips rootDirectory translationFilename TranslationFilename(ie. tips_xx.txt) [tipsFilename]");
		log("\tIf tipsFilename is missing, the default ({0}) is used.", DEFAULT_TIPS_FILENAME);
	}

	/* Logging methods. */

	/**
	 * Log a message to the standard output
	 * @param string message pattern
	 * @param o arguments
	 * @see MessageFormat#format(String, Object...)
	 */
	private static void log(String string, Object... o)
	{
		System.out.println(MessageFormat.format(string, o));
	}

	/**
	 * Log a message to the error output
	 * @param string message pattern
	 * @param o arguments
	 * @see MessageFormat#format(String, Object...)
	 */
	private static void logError(String string, Object... o)
	{
		System.err.println(MessageFormat.format(string, o));
	}
}
