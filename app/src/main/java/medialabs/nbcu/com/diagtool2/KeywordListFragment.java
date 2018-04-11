package medialabs.nbcu.com.diagtool2;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

import medialabs.nbcu.com.diagtool2.data.Keyword;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class KeywordListFragment extends Fragment {


    private static String TAG = "KeywordListFragment";
    private static final String ARG_CHANNEL_NAME = "channel-name";
    private String mChannelName = "CNBC";
    private OnListFragmentInteractionListener mListener;
    private OnGraphicsChangeEvent mGraphicsChangeListener;

    ArrayList<Keyword> mKeywordList = new ArrayList<>();
    KeywordListRecyclerViewAdapter mAdapter;
    String mCurrentGraphicsId = null;

    private WebSocketClient mWebSocketClient;
    String msgtest[]={"",""};

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public KeywordListFragment() {
    }

    public static KeywordListFragment newInstance(String channelName) {
        KeywordListFragment fragment = new KeywordListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHANNEL_NAME, channelName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mChannelName = getArguments().getString(ARG_CHANNEL_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keyword_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new KeywordListRecyclerViewAdapter(mKeywordList, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    /*private class KeywordListCallback extends SortedList.Callback<Keyword> {

        @Override
        public int compare(Keyword o1, Keyword o2) {
//            return Date.compare(o2.triggerTime,o1.triggerTime);
//            return Long.compare(o2.ts,o1.ts);
            return 0;
        }

        @Override
        public void onChanged(int position, int count) {

        }

        @Override
        public boolean areContentsTheSame(Keyword oldItem, Keyword newItem) {
            return false;
        }

        @Override
        public boolean areItemsTheSame(Keyword item1, Keyword item2) {
            return false;
        }

        @Override
        public void onInserted(int position, int count) {
           mAdapter.notifyItemInserted(position);
        }

        @Override
        public void onRemoved(int position, int count) {

        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {

        }
    }*/


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
        connectWebSocket();
        if (context instanceof OnGraphicsChangeEvent) {
            mGraphicsChangeListener = (OnGraphicsChangeEvent) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnGraphicsChangeEvent");
        }
    }


    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://23.21.253.2:9080/epi/ws/mq");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri, new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.d(TAG, "Websocket Opened");
                //mWebSocketClient.send("HELLO_FROM_ANDROID_APP");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                final FragmentActivity fragmentActivity = getActivity();
                if (fragmentActivity != null) {
                    fragmentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                msgtest = message.split(":");
                                //if (msgtest[0].equals("RTKWS2") && msgtest[1].equalsIgnoreCase(mChannelName)) {
                                if (msgtest[0].equals("RTKWS") && msgtest[1].equalsIgnoreCase(mChannelName)) {
                                    if(msgtest[1].equalsIgnoreCase("CNBC"))
                                        return;
                                    Log.d(TAG, message);
                                    String word = msgtest[2];
                                    double startTime = Double.parseDouble(msgtest[3]);
                                    double endTime = Double.parseDouble(msgtest[4]);
                                    double confidence = Double.parseDouble(msgtest[5]);
                                    long ts = Long.parseLong(msgtest[6]);
                                    String ner = msgtest[7];
                                    Date date = new Date(ts * 1000L);
                                    mKeywordList.add(0,new Keyword(word, startTime, endTime, confidence, date,ts,ner,""));
                                    mAdapter.notifyDataSetChanged();

//                                } else if(mChannelName.equalsIgnoreCase("CNBC") && msgtest[0].equals("RELATED2") && msgtest[1].equals("MATCH")){
                                } else if(mChannelName.equalsIgnoreCase("CNBC") && msgtest[0].equals("RELATED") && msgtest[1].equals("MATCH")){
                                    Log.d(TAG,message);
                                    String id = msgtest[2];
                                    String keyword = msgtest[3];
                                    String title = msgtest[4];
                                    mKeywordList.add(0,new Keyword("MATCHED", 0, 0, 0, new Date(),0,keyword,id));
                                    mAdapter.notifyDataSetChanged();

                                } else if(mChannelName.equalsIgnoreCase("CNBC") && msgtest[0].equals("RELATED") && msgtest[1].equals("DATA")  && msgtest[2].equals("566")){
                                    String title = msgtest[3];
                                    String id = msgtest[4].substring(0,msgtest[4].length() - 4);
                                    mCurrentGraphicsId = id;
                                    Log.d(TAG,"DATA:: "  + id + " title: " + title);
                                } else if(mChannelName.equalsIgnoreCase("CNBC") && msgtest[0].equals("RELATED") && msgtest[1].equals("SHOW")  && msgtest[2].equals("566")  && msgtest[3].equals("true")) {
                                    mGraphicsChangeListener.onGraphicsShow(mCurrentGraphicsId);
                                } else if(mChannelName.equalsIgnoreCase("CNBC") && msgtest[0].equals("RELATED") && msgtest[1].equals("HIDE")  && msgtest[2].equals("RELATED")  && msgtest[3].equals("true")) {
                                    mGraphicsChangeListener.onGraphicsHide();
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Error:msg " + e);
                            }
                        }
                    });
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.d(TAG, "Websocket Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "Websocket Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mGraphicsChangeListener = null;
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Keyword item);
    }

    public interface OnGraphicsChangeEvent {
        void onGraphicsShow(String id);
        void onGraphicsHide();
    }
}
