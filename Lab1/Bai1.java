import java.util.Random;
import java.util.Scanner;
public class Bai1 {
  public static void main(String[] args) {
    //Nhập bán kính
    int r;
    System.out.println("Nhap bán kính R: ");
    Scanner scanner = new Scanner(System.in);
    r = scanner.nextInt();
    //Gán N
    int N = 500000;
    //đếm số đá trong hình tròn
    int n = 0;
    Random random = new Random();

    for (int i =0; i<N ;i++){
        //Random tọa độ điểm và tính khoảng cách
        double x = random.nextDouble();
        double y = random.nextDouble();
        if(x*x + y*y <= r*r)  {
            n++;
        }
    }

    double S = (double) n / N * 4 * r * r;
    System.out.println("Diện tích hình tròn: " + S);

  }
}