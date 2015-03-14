package org.linkality.bibtex2dot;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import bibtex.dom.BibtexAbstractEntry;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import bibtex.dom.BibtexString;
import bibtex.expansions.CrossReferenceExpander;
import bibtex.expansions.ExpansionException;
import bibtex.expansions.MacroReferenceExpander;
import bibtex.expansions.PersonListExpander;
import bibtex.parser.BibtexParser;
import bibtex.parser.ParseException;

/**
 * BibTeX2Dot main class.
 * <p>
 * Creates dot graph files from BibTeX databases.
 * 
 * @author Julian Schütte
 *
 */
public class Bibtex2Dot {

	/**
	 * Display usage instructions.
	 */
	public static void usage() {
		System.err
				.println("\nSyntax: java -jar bibtex2dot.jar <input file> <output file> [keywords | authors]");
	}

	/**
	 * Main method for calling BibTeX2Dot.
	 * @param args
	 * @throws IOException
	 * @throws ParseException 
	 * @throws ExpansionException 
	 */
	public static void main(String[] args) throws IOException, ParseException, ExpansionException {
		if (args.length!=3) {
			usage();
			System.exit(-1);
		}
		generate(args);
	}
	
	public static void generate(String[] args) throws IOException, ParseException, ExpansionException {
		if (args.length < 1) {
			usage();
			return;
		}
		BibtexFile bibtexFile = new BibtexFile();
		BibtexParser parser = new BibtexParser(false);
		boolean expandMacros = false;
		boolean dropMacros = false;
		boolean expandCrossrefs = false;
		boolean expandPersonLists = false;
		boolean noOutput = false;
		for (int argsIndex = 0; argsIndex < args.length - 1; argsIndex++) {
			String argument = args[argsIndex];
			if (argument.equals("-expandStringDefinitions")) {
				expandMacros = true;
			} else if (argument.equals("-expandAndDropStringDefinitions")) {
				expandMacros = dropMacros = true;
			} else if (argument.equals("-expandCrossReferences")) {
				expandCrossrefs = expandMacros = true;
			} else if (argument.equals("-expandPersonLists")) {
				expandPersonLists = expandMacros = true;
			} else if (argument.equals("-noOutput")) {
				noOutput = true;
			} 
		}

		String filename = args[0];
		System.out.println("Parsing \"" + filename + "\" ... ");
		parser.parse(bibtexFile, new FileReader(filename));
		
		if (expandMacros) {
			System.err.println("\n\nExpanding macros ...");
			MacroReferenceExpander expander = new MacroReferenceExpander(
					true, true, dropMacros, false);
			expander.expand(bibtexFile);
			// printNonFatalExceptions(expander.getExceptions());

		}
		if (expandCrossrefs) {
			System.err.println("\n\nExpanding crossrefs ...");
			CrossReferenceExpander expander = new CrossReferenceExpander(
					false);
			expander.expand(bibtexFile);
			// printNonFatalExceptions(expander.getExceptions());
		}
		if (expandPersonLists) {
			System.err.println("\n\nExpanding person lists ...");
			PersonListExpander expander = new PersonListExpander(true,
					true, false);
			expander.expand(bibtexFile);
			// printNonFatalExceptions(expander.getExceptions());
		}

		if (noOutput)
			return;

		BufferedWriter out = new BufferedWriter(new FileWriter(args[1]));
		out.write("graph bibliography {\n");
		out.write("size=\"7,7\";\n");
		out.write("ratio=\"0.8\";\n");
		out.write("dpi=\"300\";\n");
		out.write("penwidth=\"5\";\n");
		out.write("graph [truecolor rotate=90 splines=true]\n");
		out.write("edge [dir=none]\n");
		//out.write("node [color=lightblue2, style=filled];\n");
		
		GraphFile graphFile = new GraphFile();
		List entries = bibtexFile.getEntries();
		Iterator it = entries.iterator();
		while (it.hasNext()) {
			BibtexAbstractEntry raw = (BibtexAbstractEntry) it.next();
			if (raw instanceof BibtexEntry) {
				BibtexEntry entry = (BibtexEntry) raw;
				BibtexString titleString = ((BibtexString) entry
						.getFieldValue("title"));
				if (titleString != null) {
					String title = titleString.getContent();
					title = title.trim();
					title = title.replace('.', '_');
					title = title.replace('-', '_');
					title = title.replace("{", "");
					title = title.replace("}", "");
					title = title.replace(',', ' ');
					title = title.replace('+', ' ');
					title = title.replace(" ", "");
					title = title.replace(":", "");
					title = title.replace("(", "");
					title = title.replace(")", "");
					title = title.replace("\n", "");
					title = title.replace("\r", "");
					title = title.replace("\t", "");
					if (title.length()>8)
						title = title.substring(0, 8);
					List keywords = entry.getFieldValuesAsList("keywords");
					for (int i = 0; i < keywords.size(); i++) {
						BibtexString keyword = (BibtexString) keywords.get(i);
						String keyList = keyword.getContent().replace("{", "");
						keyList.replace("}", "");
						keyList = keyList.trim();
						keyList = keyList.replace(" ", "");
						keyList = keyList.replace('-', '_');
						keyList = keyList.replace('/', '_');
						keyList = keyList.replace('(', '_');
						keyList = keyList.replace("\n", "_");
						keyList = keyList.replace("\r", "");
						keyList = keyList.replace("\t", "");
						keyList = keyList.replace(')', '_');
						keyList = keyList.replace('.', '_');
						String[] keys = keyList.split(",");
						for (int j = 0; j < keys.length; j++) {
							for (int k = j+1; k < keys.length; k++) {
								if (keys[j].trim().length()>0 &&keys[k].trim().length()>0)
									graphFile.addNode(keys[j].trim(), keys[k].trim());
							}
						}
					}
				}
			}
		}
		System.out.println("Parsing finished.");
		System.out.println("Writing " + args[1]);
		out.write(graphFile.toString());
		out.write("}\n");
		out.flush();
		out.close();
		System.out.println("Finished. Use dot, neato or fdp to create images from " + args[1]);
	}

}
