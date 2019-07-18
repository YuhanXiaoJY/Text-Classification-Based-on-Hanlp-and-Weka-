import javax.sound.midi.SysexMessage;
import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.*;
import java.net.SocketOption;
import java.nio.Buffer;
import java.util.*;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.seg.*;
import com.hankcs.hanlp.seg.NShort.*;

public class Text {
    public class Feature
    {
        Map<String, Integer> word_num;
        Map<String, Double> word_freq;
        int tag;
        String name;
        int total_num;

        Feature()
        {
            word_num = new HashMap<String, Integer>();
            word_freq = new HashMap<String, Double>();
            tag = -1;
            name = null;
            total_num = 0;
            int tmplen = feature_list.size();
            for(int i = 0; i< tmplen; i++)
            {
                word_num.put(feature_list.get(i), 0);
            }

        }
    }


    String SetFile;
    List<String> SetList;
    List<Feature> text_feature;
    List<String> feature_list;
    List<String> stop_words_list;
    List<Integer> tag_list;

    Text(String a)
    {
        SetFile = a;
        SetList = new ArrayList<String>();
        feature_list = new ArrayList<String>(Arrays.asList("之","其","或","亦","方","于","即","皆","因","仍","故","尚",
                "呢","了","的","着","一","不","乃","呀", "吗","咧","啊","把","让","向","往","是","在","越","再","更",
                "比","很","偏","别","好","可","便","就","但","儿", "又","也","都","要","这","那","你","我","他","来",
                "去","道","说"));
        stop_words_list = new ArrayList<String>(Arrays.asList("．", "：", "，", "\"", "“", "”","《", "》","？", "?",
                "。", "……", "'","’","‘", "*", "#", ",", ".", "`", "·", "<", ">", "；", ";", "[", "]","{","}", "【"
        ,"】","\\","、","|", "=", "-","+","——","_","！","!","@","#","$","%","^","&"));
        text_feature = new ArrayList<Feature>();
        tag_list = new ArrayList<Integer>();
    }

    public void GetTextFeature() throws IOException
    {
        String filename = SetFile;
        BufferedReader pathfile = new BufferedReader(new FileReader(filename));
        String path = pathfile.readLine();
        while(path != null)
        {
            path = path.strip();
            String tag = path.substring(0, 1);
            String true_path = path.substring(2);
            String[] tmp = true_path.split("/");
            String text_name = tmp[2];
            Feature f = new Feature();
            f.tag = Integer.parseInt(tag);
            tmp = text_name.split("\\.");
            f.name = tmp[0];

            BufferedReader datafile = new BufferedReader(new FileReader(true_path));
            String text_line = datafile.readLine();
            int total_num = 0;
            int line_num = 0;
            List<Term> term_list = null;
            Segment segment = new NShortSegment();
            while(text_line != null)
            {
                if(line_num > 1)
                {
                    text_line = text_line.strip();
                    term_list = segment.seg(text_line);

                    int len = term_list.size();
                    for(int i = 0; i < len; i++)
                    {
                        String word = term_list.get(i).word;
                        if(stop_words_list.contains(word))
                            continue;
                        total_num++;        //不计入停用词

                        if(!feature_list.contains(word))
                            continue;
                        if(f.word_num.containsKey(word))
                            f.word_num.put(word, f.word_num.get(word) + 1);
                    }
                }
                text_line = datafile.readLine();
                line_num++;
            }
            f.total_num = total_num;
            for(Map.Entry<String, Integer> entry: f.word_num.entrySet())
            {
                f.word_freq.put(entry.getKey(), (double)(entry.getValue())/(double)(f.total_num));
            }
            
            text_feature.add(f);
            path = pathfile.readLine();
        }

        pathfile.close();
    }

    public void WriteArff(String cmd) throws IOException
    {
        String filename = null;
        if(cmd.compareTo("train") == 0)
            filename = "train_classification.arff";
        else if(cmd.compareTo("test") == 0)
            filename = "test_classification.arff";
        BufferedWriter arff = new BufferedWriter(new FileWriter(filename));
        String str = "@relation text_classification\n";
        arff.write(str);
        arff.write("\n");
//        str = "@attribute text_name string\n";
//        arff.write(str);
        int len = feature_list.size();
        for(int i = 0;i < len; i++)
        {
            str = String.format("@attribute %s real\n",feature_list.get(i));
            arff.write(str);
        }
        str = "@attribute classification {0,1,2}\n";
        arff.write(str);
        arff.write("\n");
        arff.write("@data\n");

        //这里存词频
        int text_feature_len = text_feature.size();
        for(int j = 0;j < text_feature_len; j++)
        {
            str = "";
            Feature f = text_feature.get(j);
            int tag = f.tag;

            for(int i = 0; i < len; i++)
            {
                str = str.concat(String.format("%f,", f.word_freq.get(feature_list.get(i))));
            }
            str = str.concat(String.format("%d", tag));
            tag_list.add(tag);
            str = str.concat(String.format("\n"));
            arff.write(str);    //测试集打tag
        }

        arff.close();
    }


    public void GetSetList() throws IOException
    {
        BufferedReader set_reader = new BufferedReader(new FileReader(SetFile));

        String set_str = set_reader.readLine();
        while(set_str != null)
        {
            set_str = set_str.strip();
            //System.out.println(train_str);
            SetList.add(set_str);
            set_str = set_reader.readLine();
        }

        set_reader.close();
    }

}
