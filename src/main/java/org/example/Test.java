import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

class Test {
	public static void main(String[] args) {
		// System.out.println(isPalindrome("A man, a plan, a canal: Panama"));
		// System.out.println(isPalindrome("race a car"));
		// System.out.println(isPalindrome(" "));
		// System.out.println(isPalindrome("ab_a"));
		// System.out.println(Character.isLetterOrDigit("a".charAt(0)));
		// System.out.println("A".codePointAt(0));
		// System.out.println("A" + (char) 66);
		// String n = "";
		int columnNumber = 27;
		String n = "";
		boolean first = true;
		while (columnNumber > 0) {
		    n += (char) ((columnNumber % 27) + (first ? 64 : 65));
		    first = false;
		    columnNumber /= 27;
		}
		System.out.println(new StringBuilder(n).reverse());
		// System.out.println((char) 65);
		// System.out.println((char) 65);
		// return n;
	}

	public static boolean isPalindrome(String s) {
		// return true;
		String lowerCaseWithoutNonAlphaNumeric = s.replaceAll("[^\\w\\d]", "").replace("_", "");
		return lowerCaseWithoutNonAlphaNumeric.equalsIgnoreCase(new StringBuilder(lowerCaseWithoutNonAlphaNumeric).reverse().toString());
    }

}
