import java.util.*;
import java.util.Scanner;

public class Bai4 {
    public static void main(String[] args) {
        // Nhập và đọc dữ liệu
        Scanner sc = new Scanner(System.in);

        System.out.println("Nhap n va k");
        System.out.println("Nhap day n");

        String line1 = sc.nextLine();
        String line2 = sc.nextLine();

        // Tách n và k
        String[] first = line1.split("[,\\s]+");
        int n = Integer.parseInt(first[0]);
        int k = Integer.parseInt(first[1]);

        // Tách dãy số
        String[] arr = line2.split("[,\\s]+");
        int[] a = new int[n];

        for (int i = 0; i < n && i < arr.length; i++) {
            a[i] = Integer.parseInt(arr[i]);
        }

        // Tạo mảng DP
        @SuppressWarnings("unchecked")
        List<Integer>[] dp = (List<Integer>[]) new ArrayList[k + 1];

        dp[0] = new ArrayList<>();

        for (int num : a) {
            if (num <= 0 || num > k) continue;
            for (int s = k; s >= num; s--) {
                if (dp[s - num] != null) {
                    int newLength = dp[s - num].size() + 1;
                    if (dp[s] == null || newLength > dp[s].size()) {
                        List<Integer> newList = new ArrayList<>(dp[s - num]);
                        newList.add(num);
                        dp[s] = newList;
                    }
                }
            }
        }

        // In kết quả
        if (dp[k] != null) {
            StringJoiner result = new StringJoiner(",");
            for (int x : dp[k]) {
                result.add(String.valueOf(x));
            }
            System.out.println("Ket qua: " + result);

        } else {
            System.out.println("Ket qua: khong tim thay day so thoa man");
        }

        sc.close();
    }
}