package com.gmail.inverseconduit.javadoc;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

/**
 * Iterates through the description section of a class's Javadoc HTML page,
 * converting the description to SO-Chat-flavorted mardkdown.
 * @author Michael Angstadt
 */
public class DescriptionNodeVisitor implements NodeVisitor {
	private final StringBuilder sb = new StringBuilder();
	private boolean inPre = false;
	private String prevText;

	@Override
	public void head(Node node, int depth) {
		//for (int i = 0; i < depth; i++) {
		//	System.out.print(' ');
		//}
		//System.out.println("head " + node.nodeName());
		//if (node instanceof TextNode) {
		//	for (int i = 0; i < depth; i++) {
		//		System.out.print(' ');
		//	}
		//	System.out.println(((TextNode) node).text());
		//}

		switch (node.nodeName()) {
		case "code":
			sb.append("`");
			break;
		case "i":
		case "em":
			sb.append("*");
			break;
		case "b":
		case "strong":
			sb.append("**");
			break;
		case "br":
		case "p":
			sb.append("\n");
			break;
		case "pre":
			inPre = true;
			sb.append("\n");
			break;
		case "#text":
			TextNode text = (TextNode) node;
			String content = inPre ? text.getWholeText() : text.text();

			//in the jsoup javadocs, it's reading some text nodes twice for some reason
			if (prevText != null && prevText.equals(content)) {
				prevText = null;
			} else {
				prevText = content;
				sb.append(content);
			}
			break;
		}
	}

	@Override
	public void tail(Node node, int depth) {
		//for (int i = 0; i < depth; i++) {
		//	System.out.print(' ');
		//}
		//System.out.println("tail " + node.nodeName());

		switch (node.nodeName()) {
		case "code":
			sb.append("`");
			break;
		case "i":
		case "em":
			sb.append("*");
			break;
		case "b":
		case "strong":
			sb.append("**");
			break;
		case "p":
			sb.append("\n");
			break;
		case "pre":
			inPre = false;
			sb.append("\n");
			break;
		}
	}

	/**
	 * Gets the {@link StringBuilder} used to hold the description.
	 * @return the string builder.
	 */
	public StringBuilder getStringBuilder() {
		return sb;
	}
}