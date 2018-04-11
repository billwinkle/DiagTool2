package medialabs.nbcu.com.diagtool2;

import android.graphics.Color;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import medialabs.nbcu.com.diagtool2.KeywordListFragment.OnListFragmentInteractionListener;
import medialabs.nbcu.com.diagtool2.data.Keyword;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import java.text.SimpleDateFormat;


public class KeywordListRecyclerViewAdapter extends RecyclerView.Adapter<KeywordListRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<Keyword> mValues;
    private final OnListFragmentInteractionListener mListener;

    public KeywordListRecyclerViewAdapter(ArrayList<Keyword> items, OnListFragmentInteractionListener listener) {
        mValues=items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_keyword, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if(mValues.get(position).word.equals("MATCHED")){
            holder.mWordView.setTextColor(Color.YELLOW);
            holder.mNERView.setTextColor(Color.YELLOW);
        } else {
            holder.mWordView.setTextColor(Color.parseColor("#d1cfd0"));
            holder.mNERView.setTextColor(Color.parseColor("#d1cfd0"));
        }
        holder.mWordView.setText(mValues.get(position).word);
        //holder.mStartTimeView.setText(mValues.get(position).startTime + "");
        holder.mNERView.setText(mValues.get(position).ner);
        //holder.mEndTimeView.setText(mValues.get(position).endTime + "");

        double confidencePercent = mValues.get(position).confidence * 100.0;
        //double roundConfidence = Math.round((confidencePercent * 100.0) / 100.0);
        //holder.mConfidence.setText(String.format ("%.2f", confidencePercent) + "%");

        Date date= mValues.get(position).triggerTime;
        SimpleDateFormat jdf = new SimpleDateFormat("HH:mm:ss");
        jdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String javaDateString = jdf.format(date);
        //holder.mTriggerTime.setText (javaDateString);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mWordView;
        //public final TextView mStartTimeView;
        //public final TextView mEndTimeView;
        //public final TextView mConfidence;
        //public final TextView mTriggerTime;
        public final TextView mNERView;

        public Keyword mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mWordView = (TextView) view.findViewById(R.id.word);
            //mStartTimeView = (TextView) view.findViewById(R.id.starttime);
            //mEndTimeView = (TextView) view.findViewById(R.id.endtime);
            //mConfidence = (TextView) view.findViewById(R.id.confidence);
            //mTriggerTime = (TextView) view.findViewById(R.id.triggertime);
            mNERView = (TextView) view.findViewById(R.id.ner);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mWordView.getText() + "'";
        }
    }
}
