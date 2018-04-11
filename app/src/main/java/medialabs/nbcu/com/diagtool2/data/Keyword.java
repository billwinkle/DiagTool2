package medialabs.nbcu.com.diagtool2.data;

import java.util.Date;
/**
 * Created by Gaurav on 4/6/17.
 */

public class Keyword {
   public String  word;
   public  double   startTime;
    public  double  endTime;
    public  double   confidence;
    public Date triggerTime;
    public long ts;
    public String ner;
    public String id;

    public Keyword(){

    }

    public Keyword(String word, double startTime, double endTime, double confidence, Date triggerTime,long ts,String ner,String id) {
        this.word = word;
        this.startTime = startTime;
        this.endTime = endTime;
        this.confidence = confidence;
        this.triggerTime = triggerTime;
        this.ts = ts;
        this.ner = ner;
        this.id = id;
    }

}
