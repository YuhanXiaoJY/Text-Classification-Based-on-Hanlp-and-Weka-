import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLOutput;
import java.util.List;
import java.io.*;
import java.util.*;

import weka.associations.FilteredAssociationRules;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.Sourcable;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instances;


public class WekaTrain {
    public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception
    {
        Evaluation evaluation = new Evaluation(trainingSet);
        model.buildClassifier(trainingSet);
        evaluation.evaluateModel(model, testingSet);
        return evaluation;
    }
    public static double calculateAccuracy(FastVector predictions)
    {
        double correct = 0;
        int predictions_len = predictions.size();
        for(int i = 0; i < predictions_len; i++)
        {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if(np.predicted() == np.actual())
                correct++;
        }
        return 100 * correct / predictions_len;
    }

    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds)
    {
        Instances[][] split = new Instances[2][numberOfFolds];
        for(int i = 0; i < numberOfFolds; i++)
        {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }
        return split;
    }
    public static void run(String cmd) throws Exception
    {
        //读入test数据
        BufferedReader testfile = null;
        String test_filename = "test_classification.arff";
        Instances test_data = null;
        if(cmd.compareTo("test") == 0)
        {
            try
            {
                testfile = new BufferedReader(new FileReader(test_filename));
            }
            catch (FileNotFoundException ex)
            {
                System.out.println("File not found: " + test_filename);
            }
            test_data = new Instances(testfile);
            test_data.setClassIndex(test_data.numAttributes() - 1);
            testfile.close();
        }

        BufferedReader datafile = null;
        String filename = "train_classification.arff";
        try
        {
            datafile = new BufferedReader(new FileReader(filename));
        }
        catch(FileNotFoundException ex)
        {
            System.out.println("File not found: " + filename);
        }
        Instances data = new Instances(datafile);
        data.setClassIndex(data.numAttributes() - 1);

        //Do 10-split cross validation
        Instances[][] split = crossValidationSplit(data, 80);

        //Seperate split into training and testing arrays
        Instances[] train_splits = split[0];
        Instances[] test_splits = split[1];

        //Use a set of classifiers
        Classifier[] models = {
                new J48()
                //new DecisionTable(),
                //new DecisionStump()
        };
        //Run for each model
        for(int j = 0; j < models.length; j++)
        {
            FastVector predictions = new FastVector();
            int train_splits_len = train_splits.length;
            for(int i = 0; i< train_splits_len; i++)
            {
                Evaluation validation = classify(models[j], train_splits[i], test_splits[i]);
                predictions.appendElements(validation.predictions());

                //System.out.println(models[j].toString());
            }
            double accuracy = calculateAccuracy(predictions);
            System.out.println("Validation acc of " + models[j].getClass().getSimpleName() + ": "
            + String.format("%.2f%%", accuracy) + "\n-----------------------------------");
            if(cmd.compareTo("test") == 0)
            {
                int len = test_data.numInstances();
                List<Double> res = new ArrayList<Double>();
                for(int i = 0; i< len; i++)
                {
                    res.add(models[j].classifyInstance(test_data.instance(i)));
                }
                String res_filename = "[java19]HW2_1600012821.txt";
                BufferedWriter res_file = new BufferedWriter(new FileWriter(res_filename));
                for(int i = 0; i < len; i++)
                {
                    String tmp = String.format("%d\n", res.get(i).intValue());
                    res_file.write(tmp);
                }
                res_file.close();
            }

        }



    }

}
