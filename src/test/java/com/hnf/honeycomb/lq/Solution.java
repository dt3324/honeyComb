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

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;

class Solution {
    public static void main(String[] args) {
        ArrayList<UserBean> userBeans = new ArrayList<>();
        UserBean userBean = new UserBean();
        userBean.setAge(23);
        userBean.setName("zhang");
        userBeans.add(userBean);
        userBean.setName("lisi");
        System.out.println(userBeans.contains(userBean));
        userBeans.add(userBean);
        System.out.println(userBeans);
    }

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        if(nums1 == null && nums2 == null){
            return 0.0d;
        }
        int length = nums1.length + nums2.length;
        int a = 0, b = 0;
        for (int i = 1; i < (length + 1) / 2; i++) {

        }

        return 0.0;
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
