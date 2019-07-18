import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void SetTrainTestSet(String train_file_name, String test_file_name) throws IOException
    {
        BufferedWriter train_file = null;
        BufferedWriter test_file = null;
        try
        {
            train_file = new BufferedWriter(new FileWriter(train_file_name));
            test_file = new BufferedWriter(new FileWriter(test_file_name));
        }
        catch(IOException ex)
        {
            System.out.println("[SetTrainTestSet]:File cannot be open.");
        }

        for(int i = 1; i <= 80; i+=4)
        {
            int paras0 = 0;
            int paras1 = (i-1)/10 + 1;
            int paras2 = (i-1)%10 + 1;
            String str = String.format("%d|TextData/hlm/hlm%02d_%d.txt\n",paras0, paras1, paras2);
            train_file.write(str);
        }
        for(int i = 81; i <= 120; i+=2)
        {
            int paras0 = 1;
            int paras1 = (i-1)/10 + 1;
            int paras2 = (i-1)%10 + 1;
            String str = String.format("%d|TextData/hlm/hlm%02d_%d.txt\n",paras0, paras1, paras2);
            train_file.write(str);
        }

        for(int i = 1; i <= 120; i++)
        {
            int paras0 = 0;
            if(i > 80)
                paras0 = 1;
            int paras1 = (i-1)/10 + 1;
            int paras2 = (i-1)%10 + 1;
            String str = String.format("%d|TextData/hlm/hlm%02d_%d.txt\n",paras0, paras1, paras2);
            test_file.write(str);
        }
        for(int i = 1; i<= 120; i+=3)
        {
            int paras1 = (i-1)/10 + 1;
            int paras2 = (i-1)%10 + 1;
            String str = String.format("2|TextData/sgyy/sgyy%02d_%d.txt\n",paras1, paras2);
            train_file.write(str);
        }
//        for(int i = 2; i<= 120; i+=3)
//        {
//            int paras1 = (i-1)/10 + 1;
//            int paras2 = (i-1)%10 + 1;
//            String str = String.format("2|TextData/sgyy/sgyy%02d_%d.txt\n",paras1, paras2);
//            test_file.write(str);
//        }
//        for(int i = 3; i<= 120; i+=3)
//        {
//            int paras1 = (i-1)/10 + 1;
//            int paras2 = (i-1)%10 + 1;
//            String str = String.format("2|TextData/sgyy/sgyy%02d_%d.txt\n",paras1, paras2);
//            test_file.write(str);
//        }
        for(int i = 1; i<= 120; i++)
        {
            int paras1 = (i-1)/10 + 1;
            int paras2 = (i-1)%10 + 1;
            String str = String.format("2|TextData/sgyy/sgyy%02d_%d.txt\n",paras1, paras2);
            test_file.write(str);
        }
        train_file.close();
        test_file.close();
    }

    public static void TestAcc(List<Integer> tag_list) throws IOException
    {
        String[] filename = {"[java19]HW2_1600012821.txt"};
        for(int i = 0; i< filename.length; i++)
        {
            int correct = 0;
            BufferedReader file = new BufferedReader(new FileReader(filename[i]));
            List<Integer> res_list = new ArrayList<Integer>();
            String line = file.readLine();
            while(line != null)
            {
                line = line.strip();
                res_list.add(Integer.parseInt(line));
                line = file.readLine();
            }
            file.close();
            int list_len = tag_list.size();
            for(int j = 0; j < list_len; j++)
            {
                if(tag_list.get(j).equals(res_list.get(j)))
                    correct++;
                //System.out.println(tag_list.get(j) + "  " + res_list.get(j));
            }
            double acc = (float)correct / list_len;
            System.out.println("Test num: " + list_len);
            String str = String.format("Test acc of %s: %.2f%%", filename[i], 100*acc);
            System.out.println(str);
            System.out.println("-------------------------------");
        }
    }

    public static void main(String[] args) throws Exception
    {
        String TrainSet_filename = args[0];
        String TestSet_filename = args[1];
        //SetTrainTestSet(TrainSet_filename, TestSet_filename);
        Text train = new Text(TrainSet_filename);
        train.GetSetList();
        train.GetTextFeature();
        train.WriteArff("train");

        Text test = new Text(TestSet_filename);
        test.GetSetList();
        test.GetTextFeature();
        test.WriteArff("test");

        WekaTrain w = new WekaTrain();
        w.run("test");
        TestAcc(test.tag_list);
    }




}
