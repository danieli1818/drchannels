package me.danieli1818.drchannels.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentsParser {

	public static String[] parse(String[] args) {
		return parse(String.join(" ", args));
	}
	
	public static String[] parse(String args) {
		List<String> arguments = new ArrayList<String>();
		Pattern pattern = Pattern.compile("([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(?:\\\\.[^'\\\\]*)*)'|[^\\s]+");
		Matcher matcher = pattern.matcher(args);
		while (matcher.find()) {
			if (matcher.group(1) != null) {
		        // Add double-quoted string without the quotes
		        arguments.add(matcher.group(1));
		    } else if (matcher.group(2) != null) {
		        // Add single-quoted string without the quotes
		        arguments.add(matcher.group(2));
		    } else {
		        // Add unquoted word
		        arguments.add(matcher.group());
		    }
		}
		return (String[]) arguments.toArray(new String[arguments.size()]);
	}
	
}
