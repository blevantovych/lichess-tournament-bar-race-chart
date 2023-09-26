import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

class Test {
	public static void main(String[] args) {
		System.out.println(numIdenticalPairs(new int[]{1,2,3,1,1,2}));
	}

	public static Map numIdenticalPairs(int[] nums) {
		Map<Integer, List<Integer>> identicalNums = new HashMap<>();
		for (int n : nums) {
			var list = new ArrayList<Integer>(){{add(n);}};
			identicalNums.merge(n, list, (oldValue, newValue) -> Stream.concat(oldValue.stream(), newValue.stream()).collect(Collectors.toList()));
		}
		return identicalNums;
	}

}
