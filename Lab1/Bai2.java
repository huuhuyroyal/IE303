import java.util.Random;
public class Bai2 {
  public static void main(String[] args) {
    int r = 1;
    //Gán N
    int N = 500000;
    //đếm số đá trong hình tròn
    int n = 0;
    Random random = new Random();

    for (int i =0; i<N ;i++){
        //Random tọa độ điểm và tính khoảng cách
        double x = -r + 2*r * random.nextDouble();
        double y = -r + 2*r * random.nextDouble();
        if(x*x + y*y <= 1)  {
            n++;
        }
    }

    double S = (double) n / N * 4 * r * r;
    double pi = (double) S/r*r;
    System.out.println("Gia tri cua π: " + pi);

  }
}