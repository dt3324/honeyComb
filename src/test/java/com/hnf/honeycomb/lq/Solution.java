package com.hnf.honeycomb.lq;

import com.hnf.honeycomb.lq.entity.ListNode;
import com.hnf.honeycomb.lq.entity.TreeNode;
import com.hnf.honeycomb.lq.entity.UserBean;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDriverInformation;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.regex.Pattern;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;

class Solution {


//    public static void main(String[] args) {
//        //213
//        //[12,28,83,4,25,26,25,2,25,25,25,12]
//        int[] a = {};
//        System.out.println(minSubArrayLen(100,a));
//    }

//    public static String longestPalindrome(String s) {
//        int left = 0, right = 1;
//        String res = s.substring(left,right);
//        while (right <= s.length()){
//            s.
//        }
//        return "";
//    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        char[][] chars = new char[9][9];
        chars[0] = new char[]{'5','3','.','.','7','.','.','.','.'};
        chars[1] = new char[]{'6','.','.','1','9','5','.','.','.'};
        chars[2] = new char[]{'.','9','8','.','.','.','.','6','.'};
        chars[3] = new char[]{'8','.','.','.','6','.','.','.','3'};
        chars[4] = new char[]{'4','.','.','8','.','3','.','.','1'};
        chars[5] = new char[]{'7','.','.','.','2','.','.','.','6'};
        chars[6] = new char[]{'.','6','.','.','.','.','2','8','.'};
        chars[7] = new char[]{'.','.','.','4','1','9','.','.','5'};
        chars[8] = new char[]{'.','.','.','.','8','.','.','7','9'};
        System.out.println( solution.isValidSudoku(chars));
    }
    private final int L = 9;

    public boolean isValidSudoku(char[][] board) {
        boolean[][] rows = new boolean[L][L];
        boolean[][] cols = new boolean[L][L];
        boolean[][] boxes = new boolean[L][L];

        for (int r = 0; r < L; ++r) {
            for (int c = 0; c < L; ++c) {
                if (board[r][c] != '.') {
                    int value = board[r][c] - '1';
                    int boxIndex = r / 3 * 3 + c / 3;
                    if (rows[r][value] || cols[c][value] || boxes[boxIndex][value]) {
                        return false;
                    }
                    rows[r][value] = true;
                    cols[c][value] = true;
                    boxes[boxIndex][value] = true;
                }
            }
        }
        return true;
    }

    public int searchInsert(int[] nums, int target) {
        if(nums.length < 1) return 0;
        if(nums.length == 1){
            return nums[0] < target ? 1 : 0;
        }
        int left = 0, right = nums.length - 1;
        while (left <= right){
            int mod = left + (right - left) / 2;
            if(nums[mod] < target){
                left = mod + 1;
            }else {
                right = mod - 1;
            }
        }
        return left;
    }

    public int searchInsert1(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            if(nums[i] >= target){
                return i;
            }
        }
        return nums.length;
    }

    public int[] searchRange(int[] nums, int target) {
        if(nums.length < 1) return new int[]{-1, -1};
        if(nums.length == 1){
            if(nums[0] == target) return new int[]{0, 0};
            return new int[]{-1, -1};
        }
        int[] ints = new int[2];
        int left = getLeftBounf(nums, target);
        ints[0] = left;
        int right = getRightBound(nums, target);
        ints[1] = right;
        return ints;
    }

    private int getRightBound(int[] nums, int target) {
        int left = 0, right = nums.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (nums[mid] == target) {
                left = mid + 1;
            }else if (nums[mid] < target) {
                left = mid + 1;
            }else if (nums[mid] > target) {
                right = mid - 1;
            }
        }
        if(right < 0 || nums[right] != target) return -1;
        return right;
    }

    private int getLeftBounf(int[] nums, int target) {
        int left = 0, right = nums.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (nums[mid] == target) {
                right = mid - 1;
            }else if (nums[mid] < target) {
                left = mid + 1;
            }else if (nums[mid] > target) {
                right = mid - 1;
            }
        }
        if(left > nums.length - 1 || nums[left] != target) return -1;
        return left;
    }

    public int longestValidParentheses(String s) {
        int res = 0;
        if(s.length() < 2) return res;
        Stack<Integer> integers = new Stack<>();
        integers.push(-1);
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '('){
                integers.push(i);
            }
            if(s.charAt(i) == ')'){
                integers.pop();
                if(integers.empty()){
                    integers.push(i);
                }else {
                    res = res > i - integers.peek() ? res : i - integers.peek();
                }
            }
        }
        return res;
    }

    public int kthSmallest(int[][] matrix, int k) {
        PriorityQueue<Integer> integers = new PriorityQueue<>(Collections.reverseOrder());
        for (int[] ints : matrix) {
            for (int anInt : ints) {
                if(integers.size() == k && anInt > integers.peek()) break;
                integers.add(anInt);
                if(integers.size() > k) integers.remove();
            }
        }
        return integers.peek();
    }

    public int kthSmallest1(int[][] matrix, int k) {
        int[] ints = new int[matrix.length + matrix[0].length];
        int index = 0;
        for (int[] matrix1 : matrix) {
            for (int i : matrix1) {
                ints[index] = i;
                index++;
            }
        }
        Arrays.sort(ints);
        return ints[k-1];
    }

    public void nextPermutation(int[] nums) {
        int i = nums.length - 2;
        while (i >= 0 && nums[i + 1] <= nums[i]) {
            i--;
        }
        if (i >= 0) {
            int j = nums.length - 1;
            while (j >= 0 && nums[j] <= nums[i]) {
                j--;
            }
            swap(nums, i, j);
        }
        reverse(nums, i + 1);
        System.out.println(Arrays.toString(nums));
    }

    private void reverse(int[] nums, int start) {
        int i = start, j = nums.length - 1;
        while (i < j) {
            swap(nums, i, j);
            i++;
            j--;
        }
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public List<Integer> findSubstring(String s, String[] words) {
        ArrayList<Integer> list = new ArrayList<>();
        if(s.equals("") || words.length < 1) return list;
        int count = words[0].length();
        for (int i = 0; i < s.length() - count * words.length + 1; i++) {
            String substring = s.substring(i, i + count * words.length);
            ArrayList<String> strs = new ArrayList<>();
            for (int j = 0; j < substring.length(); j += count) {
                strs.add(substring.substring(j, count + j));
            }
            int j;
            for (j = 0; j < words.length; j++) {
                if (strs.contains(words[j])) {
                    strs.remove(words[j]);
                }else {
                    break;
                }
            }
            if(j == words.length){
                list.add(i);
            }
        }
        return list;
    }

    public int divide(int dividend, int divisor) {
        if(dividend == 0) return 0;
        int res = 0;
        int  b = 1;
        if(divisor > 0) {
            divisor = -divisor;
            b = -b;
        }
        if(dividend > 0) {
            b = -b;
            dividend = -dividend;
        }
        while (dividend - divisor <= divisor){
            res++;
            dividend -= divisor;
        }
        if(b < 0) return  - (res + 1);
        res = res > Integer.MAX_VALUE - 1 ? Integer.MAX_VALUE : res + 1;
        return res;
    }

    public int divide1(int dividend, int divisor) {
        boolean sign = (dividend > 0) ^ (divisor > 0);
        int result = 0;
        if(dividend>0) {
            dividend = -dividend;
        }
        if(divisor>0) divisor = -divisor;

        while(dividend <= divisor) {
            int temp_result = -1;
            int temp_divisor = divisor;
            while(dividend <= (temp_divisor << 1)) {
                if(temp_divisor <= (Integer.MIN_VALUE >> 1))break;
                temp_result = temp_result << 1;
                temp_divisor = temp_divisor << 1;
            }
            dividend = dividend - temp_divisor;
            result += temp_result;
        }
        if(!sign) {
            if(result <= Integer.MIN_VALUE) return Integer.MAX_VALUE;
            result = - result;
        }
        return result;
    }

    public int strStr(String haystack, String needle) {
        if(haystack == null || needle == null || needle.equals("")) return 0;
        char[] chars = haystack.toCharArray();
        char[] needleChars = needle.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(chars[i] == needleChars[0]){
                boolean b = false;
                for (int j = 0; j < needleChars.length; j++) {
                    b = true;
                    if (j + i >= chars.length || needleChars[j] != chars[j + i]) {
                        b = false;
                        break;
                    }

                }
                if(b){
                    return i;
                }
            }
        }
        return -1;
    }

    public int removeElement(int[] nums, int val) {
        if(nums == null || nums.length < 1) return 0;
        int i = 0;
        for (int j = 0; j < nums.length; j++) {
            if(nums[j] != val){
                nums[i] = nums[j];
                i++;
            }
        }
        return i;
    }

    public int removeElement1(int[] nums, int val) {
        if(nums == null || nums.length < 1) return 0;
        Arrays.sort(nums);
        int a = nums[nums.length - 1] + 1;
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            if(nums[i] == val){
                count++;
                nums[i] = a;
            }
        }
        Arrays.sort(nums);
        return nums.length - count;
    }

    public int removeDuplicates(int[] nums) {
        if(nums.length < 2 ) return nums.length;
        int p = 0, q = 1;
        while (q < nums.length){
            if(nums[p] != nums[q]){
                nums[p + 1] = nums[q];
                p++;
            }
            q++;

        }
        return p + 1;
    }

    public int removeDuplicates1(int[] nums) {
        if(nums.length < 2 ) return nums.length;
        int a = nums[nums.length -1] + 1;
        int size = distct(nums, a ,nums.length);
        return size;
    }

    private int distct(int[] nums, int a, int s) {
        for (int i = 1; i < nums.length; i++) {
            if(nums[i] == nums[i-1]){
                nums[i] = a;
            }
        }
        Arrays.sort(nums);
        int size = 0;
        for (int num : nums) {
            if(num < a) size++;
        }
        if(size != s){
            return distct(nums,a,size);
        }else {
            return size;
        }
    }

    public ListNode swapPairs(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode next = head.next;
        head.next = swapPairs(next.next);
        next.next = head;
        return next;
    }

    public ListNode swapPairs1(ListNode head) {
        if(head == null ) return head;
        ArrayList<Integer> list = new ArrayList<>();
        do {
            list.add(head.val);
            head = head.next;
        } while (head != null);
        ListNode listNode = new ListNode(0);
        ListNode a = listNode;
        for (int i = 1; i < list.size(); i++) {
            if(i % 2 == 1){
                a.next = new ListNode(list.get(i));
                a = a.next;
                a.next = new ListNode(list.get(i - 1));
                a = a.next;
            }
        }
        if(list.size() % 2 == 1){
            a.next = new ListNode(list.get(list.size() - 1));
        }
        return listNode.next;
    }

    public int findLength(int[] A, int[] B) {
        int res = 0;
        int a = A.length;
        int b = B.length;
        if (a < 1 || b < 1) {
            return 0;
        }
        for (int i = 0; i < a - res; i++) {
            for (int j = 0; j < b - res; j++) {
                if(A[i] == B[j]){
                    int k = i, l = j, size = 0;
                    while ( k < a&& l < b &&A[k] == B[l] ){
                        size++;
                        k++;
                        l++;
                    }
                    res = Math.max(size, res);
                }
            }
        }
        return res;
    }

    public int findLength1(int[] A, int[] B) {
        int a = A.length;
        int b = B.length;
        if (a < 1 || b < 1) {
            return 0;
        }
        int[][] dp = new int[a + 1][b + 1];
        int res = 0;
        for (int i = a - 1; i >= 0; i--) {
            for (int j = b - 1; j >= 0; j--) {
                dp[i][j] = A[i] == B[j] ? dp[i + 1][j + 1] + 1 : 0;
                res = Math.max(dp[i][j], res);
            }
        }
        return res;
    }

    public static void main1(String[] args) {
        ListNode listNode1 = new ListNode(1);
        ListNode a = listNode1;
        for (int i = 3; i < 1000; i += 2) {
            a.next = new ListNode(i);
            a = a.next;
        }
        ListNode listNode2 = new ListNode(2);
        ListNode b = listNode2;
        for (int i = 4; i < 1000; i += 2) {
            b.next = new ListNode(i);
            b = b.next;
        }
        ListNode[] listNodes = {listNode1, listNode2};
        ListNode listNode = new Solution().mergeKLists(listNodes);
        do {
            System.out.println(listNode.val);
            listNode = listNode.next;
        } while (listNode != null);
    }

    public ListNode mergeKLists(ListNode[] lists) {
        long l = System.currentTimeMillis();
        if(lists.length == 0) return null;
        ListNode listNode = new ListNode(0);
        ListNode le = listNode;
        int a = 1;
        while (a > 0){
            a = 0;
            int min = Integer.MAX_VALUE, index = 0;
            for (int i = 0; i < lists.length; i++) {
                if(lists[i] != null &&lists[i].val <= min){
                    min = lists[i].val;
                    index = i;
                }
            }
            if(lists[index] != null){
                le.next = lists[index];
                lists[index] = lists[index].next;
                le = le.next;
            }else {
                lists[index] = null;
            }
            for (ListNode list : lists) {
                if(list != null){
                    a++;
                }
            }
        }
        System.out.println(System.currentTimeMillis() - l);
        return listNode.next;
    }

    public List<String> generateParenthesis(int n) {
        List<String> list = new ArrayList<>();
        dfs("",n,n,list);
        return list;
    }

    private void dfs(String s , int left ,int right ,List<String> res){
        if(left == 0 && right == 0){
            res.add(s);
            return;
        }
        if(left>right) return;
        if(left > 0){
            dfs(s + "(",left -1,right,res);
        }
        if(right > 0){
            dfs(s + ")", left, right - 1 , res);
        }
    }

    public static ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        ListNode listNode = new ListNode(0);
        ListNode l = listNode;
        while (l1 != null || l2 != null){
            if (l2 == null) {
                l.next = l1;
                break;
            }else if (l1 == null){
                l.next = l2;
                break;
            }
            int val1 = l1.val;
            int val2 = l2.val;
            if(val1 <= val2){
                l.next = l1;
                l = l.next;
                l1 = l1.next;
            }else {
                l.next = l2;
                l = l.next;
                l2 = l2.next;
            }
        }
        return listNode.next;
    }

    public static boolean isValid(String s) {
        if(s.length() % 2 == 1) return false;
        Stack<Integer> characters = new Stack<>();
        char[] chars = s.toCharArray();
        List<Character> left = Arrays.asList('(', '[', '{');
        List<Character> right = Arrays.asList(')', ']', '}');
        for (char aChar : chars) {
            if(left.contains(aChar)){
                int i = left.indexOf(aChar);
                characters.push(i);
            }else {
                if (characters.empty() || characters.pop() != right.indexOf(aChar)) {
                    return false;
                }
            }
        }
        if(!characters.empty()) return false;
        return true;
    }

    public static ListNode removeNthFromEnd(ListNode head, int n) {
        if (head.next == null) return null;
        int length = 1;
        ListNode l = head;
        while (l.next != null) {
            length++;
            l = l.next;
        }
        ListNode res = new ListNode(0);
        res.next = head;
        l = res;
        for (int i = 0; i < length - n; i++) {
            l = l.next;
        }
        l.next = l.next.next;
        return res.next;
    }

    public static int romanToInt(String s) {
        List<String> LM = Arrays.asList("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I");
        List<Integer> math = Arrays.asList(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1);
        int res = 0;
        for (int i = 0; i < LM.size(); i++) {
            while (s.startsWith(LM.get(i))) {
                res += math.get(i);
                s = s.substring(LM.get(i).length());
            }
        }
        return res;
    }

    public static String intToRoman(int num) {
        List<String> LM = Arrays.asList("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I");
        List<Integer> math = Arrays.asList(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < math.size(); i++) {
            while (num - math.get(i) >= 0) {
                builder.append(LM.get(i));
                num = num - math.get(i);
            }
        }
        return builder.toString();
    }

    public static boolean isMatch(String s, String p) {
        if (s == null || p == null) {
            return false;
        }
        boolean[][] dp = new boolean[s.length() + 1][p.length() + 1];
        dp[0][0] = true;//dp[i][j] 表示 s 的前 i 个是否能被 p 的前 j 个匹配
        for (int i = 0; i < p.length(); i++) { // here's the p's length, not s's
            if (p.charAt(i) == '*' && dp[0][i - 1]) {
                dp[0][i + 1] = true; // here's y axis should be i+1
            }
        }
        for (int i = 0; i < s.length(); i++) {
            for (int j = 0; j < p.length(); j++) {
                if (p.charAt(j) == '.' || p.charAt(j) == s.charAt(i)) {//如果是任意元素 或者是对于元素匹配
                    dp[i + 1][j + 1] = dp[i][j];
                }
                if (p.charAt(j) == '*') {
                    if (p.charAt(j - 1) != s.charAt(i) && p.charAt(j - 1) != '.') {//如果前一个元素不匹配 且不为任意元素
                        dp[i + 1][j + 1] = dp[i + 1][j - 1];
                    } else {
                        dp[i + 1][j + 1] = (dp[i + 1][j] || dp[i][j + 1] || dp[i + 1][j - 1]);
                            /*
                            dp[i][j] = dp[i-1][j] // 多个字符匹配的情况
                            or dp[i][j] = dp[i][j-1] // 单个字符匹配的情况
                            or dp[i][j] = dp[i][j-2] // 没有匹配的情况
                             */

                    }
                }
            }
        }
        return dp[s.length()][p.length()];
    }

    public static boolean isPalindrome1(int x) {
        if (x < 0) return false;
        if (x < 10) return true;
        int old = x;
        int res = 0;
        do {
            res = res * 10 + x % 10;
            x = x / 10;
        } while (x > 0);
        return old == res;
    }

    public static boolean isPalindrome(int x) {
        String s = x + "";
        char[] chars = s.toCharArray();
        int l = 0, r = s.length() - 1;
        while (l < r) {
            if (chars[l] != chars[r]) {
                return false;
            }
            l++;
            r--;
        }
        return true;
    }

    public static int myAtoi(String str) {
        str = str.trim();
        if (str.equals("") || (!str.startsWith("+") && !str.startsWith("-") && !Character.isDigit(str.charAt(0))))
            return 0;
        boolean b = false;
        String start = null;
        if (str.startsWith("+") || str.startsWith("-")) {
            b = true;
            start = str.substring(0, 1);
            str = str.substring(1);
        }
        while (str.startsWith("0")) str = str.substring(1);
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                break;
            }
            stringBuilder.append(c);
        }
        String s = stringBuilder.toString();
        if (s.equals("")) return 0;
        if (s.length() > 10) s = s.substring(0, 11);
        long aLong;
        if (b) {
            aLong = Long.valueOf(start + s);
        } else {
            aLong = Long.valueOf(s);
        }
        if (aLong < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        if (aLong > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) aLong;
    }

    public static int reverse(int x) {
        if (x > -10 && x < 10) {
            return x;
        }
        String s = x + "";
        while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
        boolean b = false;
        if (s.startsWith("-")) {
            s = s.substring(1);
            b = true;
        }
        if (b) {
            Long aLong = Long.valueOf("-" + new StringBuilder(s).reverse().toString());
            if (aLong < Integer.MIN_VALUE) return 0;
            return Integer.valueOf("-" + new StringBuilder(s).reverse().toString());
        }
        Long aLong = Long.valueOf(new StringBuilder(s).reverse().toString());
        if (aLong > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Integer.valueOf(new StringBuilder(s).reverse().toString());
    }

    public static String convert(String s, int numRows) {
        if (numRows == 1) {
            return s;
        }
        ArrayList<StringBuilder> lists = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            lists.add(new StringBuilder());
        }
        int a = 2, size = 0;
        for (int i = 0; i < s.length(); i++) {
            lists.get(size).append(s.charAt(i));
            if (a % 2 == 0) {
                size++;
                if (size >= numRows) {
                    a++;
                    size -= 2;
                }
            } else {
                size--;
                if (size < 0) {
                    a++;
                    size += 2;
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (StringBuilder sb : lists) {
            stringBuilder.append(sb);
        }
        return stringBuilder.toString();
    }

    public static String longestPalindrome(String s) {
        long l = System.currentTimeMillis();
        if (s.equals("") || s.length() == 1) return s;
        String res = s.substring(0, 1);
        for (int i = 0; i < s.length(); i++) {
            for (int j = i + res.length(); j < s.length(); j++) {
                String substring = s.substring(i, j + 1);
                if (validPalindromic(substring)) {
                    res = res.length() > substring.length() ? res : substring;
                }
            }
        }
        System.out.println(System.currentTimeMillis() - l);
        return res;
    }

    private static boolean validPalindromic(String s) {
        char[] chars = s.toCharArray();
        int left = 0, right = s.length() - 1;
        while (left < right) {
            if (chars[left] != chars[right]) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    public static int findKthLargest(int[] nums, int k) {
        Arrays.sort(nums);
        return nums[nums.length - k];
    }

    public static int findKthLargest1(int[] nums, int k) {
        int len = nums.length;
        // 使用一个含有 k 个元素的最小堆
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(k, (a, b) -> a - b);
        for (int i = 0; i < k; i++) {
            minHeap.add(nums[i]);
            System.out.println("00000000000000" + minHeap);
        }
        for (int i = k; i < len; i++) {
            // 看一眼，不拿出，因为有可能没有必要替换
            Integer topEle = minHeap.peek();
            // 只要当前遍历的元素比堆顶元素大，堆顶弹出，遍历的元素进去
            if (nums[i] > topEle) {
                minHeap.poll();
                System.out.println(minHeap);
                minHeap.add(nums[i]);
                System.out.println(minHeap);
            }
        }
        return minHeap.peek();
    }

    public static int minSubArrayLen(int s, int[] nums) {
        if(nums.length < 1) return 0;
        int lift = 0, right = 0,sum = nums[lift],res = nums.length + 1;
        while (right < nums.length){
            if(sum >= s){
                res = res < (right - lift + 1 ) ? res : right - lift + 1;
                sum -= nums[lift];
                lift++;
            }else {
                right++;
                if(right < nums.length){
                    sum += nums[right];
                }
            }
        }
        if(res > nums.length){
            return 0;
        }
        return res;
    }

    public static int maxArea(int[] height) {
        int a = 0, b = height.length -1, res = 0;
        while (a <  b ){
            int i = (b - a) * (height[a] > height[b] ? height[b] : height[a]);
            if(height[a] > height[b]){
                res = res > i ?res : i;
                b--;
            }else{
                res = res > i ?res : i;
                a++;
            }
        }
        return res;
    }

    private static int getValue(char ch) {
        switch(ch) {
            case 'I': return 1;
            case 'V': return 5;
            case 'X': return 10;
            case 'L': return 50;
            case 'C': return 100;
            case 'D': return 500;
            case 'M': return 1000;
            default: return 0;
        }
    }

    public static String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0 || strs[0].length() < 1) {
            return "";
        }
        if(strs.length ==1) return strs[0];
        String s =  strs[0];
        a:
        for (int i = 1; i <= strs[0].length(); i++) {
            for (String str : strs) {
                if(!str.startsWith(s)){
                    s = s.substring(0, s.length() - 1);
                    continue a;
                }
            }
            return s;
        }
        return s;
    }

    public static List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(nums);
        for (int i = 0; i <  nums.length; i++) {
            if(nums[i] > 0){
                break;
            }
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }
            int start = i + 1, end = nums.length - 1;
            while (start < end){
                int sum = nums[i] + nums[start] + nums[end];
                if(sum < 0){
                    start++;
                }else if(sum > 0){
                    end--;
                }else {
                    res.add(Arrays.asList(nums[i], nums[start], nums[end]));
                    while (start < end && nums[start] == nums[start + 1]) {
                        start++;
                    }
                    while (start < end && nums[end] == nums[end - 1]) {
                        end--;
                    }
                    start++;
                    end--;
                }
            }
        }
        return res;
    }

    public static int threeSumClosest(int[] nums, int target) {
        int res = nums[0] + nums[1] + nums[2];
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < nums.length; j++) {
                for (int k = 0; k < nums.length; k++) {
                    if(i != j && j != k && i != k){
                        int sum = nums[i] + nums[j] + nums[k];
                        res = Math.abs(res - target) < Math.abs(sum - target) ? res : sum;
                    }
                }
            }
        }
        return res;
    }

    public String addBinary(String a, String b) {
        StringBuilder ans = new StringBuilder();
        int ca = 0;
        for(int i = a.length() - 1, j = b.length() - 1;i >= 0 || j >= 0; i--, j--) {
            int sum = ca;
            sum += i >= 0 ? a.charAt(i) - '0' : 0;
            sum += j >= 0 ? b.charAt(j) - '0' : 0;
            ans.append(sum % 2);
            ca = sum / 2;
        }
        ans.append(ca == 1 ? ca : "");
        return ans.reverse().toString();
    }

    public static boolean isPalindrome(String s) {
        if(s == null || s.length() == 0 || s.length() == 1){
            return true;
        }
        s = s.toLowerCase();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(Character.isLetterOrDigit(c)){
                builder.append(c);
            }
        }
        s = builder.toString();
        StringBuilder reverse = new StringBuilder(builder).reverse();
        String s1 = reverse.toString();
        return s.equals(s1);
    }

    public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
        if(nums1 == null && nums2 == null){
            return 0.0;
        }
        int[] sum;
        if(nums1 == null){
            sum = nums2;
        }else if(nums2 == null){
            sum = nums1;
        }else {
            int length = nums1.length + nums2.length;
            sum = new int[length];
            System.arraycopy(nums1, 0, sum, 0, nums1.length);
            System.arraycopy(nums2, 0, sum, nums1.length, nums2.length);
        }
        Arrays.sort(sum);
        int length = sum.length;
        if(length%2 == 1){
            return sum[length / 2];
        }
        int a = sum[length / 2] + sum[length / 2 - 1];
        return (double) a / 2;
    }

    public static int lengthOfLongestSubstring(String s) {
        if(s == null || "".equals(s) ){
            return 0;
        }
        int res = 1;
        for (int i = 0; i < s.length(); i++) {
            int z = 0;
            HashSet<Character> characters = new HashSet<>();
            characters.add(s.charAt(i));
            z++;
            for (int j = i+1; j < s.length(); j++) {
                z++;
                characters.add(s.charAt(j));
                if(characters.size() == z){
                    res = res > z ? res : z;
                }else {
                    break;
                }
            }
        }
        return res;
    }

    public static int subarraySum(int[] nums, int k) {
        if(nums == null || nums.length == 0){
            return 0;
        }
        int length = nums.length;
        int i = 0, j = 0, sum,size = 0;
        sum = nums[i];
        while (i < length && j < length) {
            if (sum == k) {
                size++;
                j++;
                if(j >= length){
                    break;
                }
                sum = sum + nums[j];
            }
            if (sum > k) {
                sum = sum - nums[i];
                i++;
            }
            if (sum < k) {
                j++;
                if(j >= length){
                    break;
                }
                sum = sum + nums[j];
            }
        }
        return size;
    }

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode res = new ListNode(0);
        ListNode temporary = res;
        int a = 0;
        while (l1 != null || l2 != null) {
            int i = 0, j = 0;
            if (l1 != null) {
                i = l1.val;
                l1 = l1.next;
            }
            if (l2 != null) {
                j = l2.val;
                l2 = l2.next;
            }
            int sum = i + j;
            if (a == 1) {
                temporary.next = new ListNode((sum + 1) % 10);
                temporary = temporary.next;
            } else {
                temporary.next = new ListNode(sum % 10);
                temporary = temporary.next;
            }
            a = sum / 10;
        }
        if (a > 0) {
            temporary.next = new ListNode(a);
        }
        return res.next;
    }

    public int singleNumber(int[] nums) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int num : nums) {
            if (list.contains(num)) {
                list.remove(list.indexOf(num));
            } else {
                list.add(num);
            }
        }
        return list.get(0);
    }

    public int[] twoSum(int[] nums, int target) {
        int[] ints = new int[2];
        if (nums == null || nums.length == 0) {
            return new int[]{};
        }
        HashMap<Integer, Integer> map = new HashMap<>();
        int length = nums.length;
        for (int i = 0; i < length; i++) {
            map.put(nums[i], i);
        }
        for (int i = 0; i < length; i++) {
            int n = target - nums[i];
            if (map.containsKey(n)) {
                if (i != map.get(n)) {
                    ints[0] = i;
                    ints[1] = map.get(n);
                    return ints;
                }
            }
        }
        return new int[]{};
    }

    public static List<List<Integer>> levelOrder(TreeNode root) {
        if (root == null)
            return new ArrayList<>();
        List<List<Integer>> res = new ArrayList<>();
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.add(root);
        while (!queue.isEmpty()) {
            int count = queue.size();
            List<Integer> list = new ArrayList<Integer>();
            while (count > 0) {
                TreeNode node = queue.poll();
                list.add(node.val);
                if (node.left != null)
                    queue.add(node.left);
                if (node.right != null)
                    queue.add(node.right);
                count--;
            }
            res.add(list);
        }
        return res;
    }

    /***
     * 给你一个二叉树，请你返回其按 层序遍历 得到的节点值。 （即逐层地，从左到右访问所有节点）。
     * @param root
     * @return
     */
    public static List<List<Integer>> levelOrder1(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null) {
            return res;
        }
        LinkedList<TreeNode> integers = new LinkedList<>();
        integers.offer(root);
        while (integers.size() > 0) {
            ArrayList<Integer> list = new ArrayList<>();
            int size = integers.size();
            while (size > 0) {
                TreeNode poll = integers.poll();
                list.add(poll.val);
                if (poll.left != null) {
                    integers.offer(poll.left);
                }
                if (poll.right != null) {
                    integers.offer(poll.right);
                }
                size--;
            }
            res.add(list);
        }
        return res;
    }
}
