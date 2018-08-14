package pgonzalezdesign.com.netsellstechtest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Request.Method.GET;

public class MainActivity extends AppCompatActivity {

    private String url = "https://www.reddit.com/r/Android/hot.json";

    private TextView subreddit;
    private List<Post> postListArray;
    private PostAdapter postListAdapter;
    private ListView postList;
    private boolean flag_loading;
    private int postListCount;
    private String pageAfter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subreddit = (TextView) findViewById(R.id.subreddit);
        subreddit.setText("HOT POST FROM ANDROID SUBREDDIT");
        postListArray = new ArrayList<>();
        postList = (ListView) findViewById(R.id.postList);
        postListAdapter = new PostAdapter(getApplicationContext(), postListArray);
        postList.setAdapter(postListAdapter);
        flag_loading = false;
        postListCount = 0;
        pageAfter = "";

        getData(url);

        postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String urlString = postListArray.get(i).getUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                startActivity(browserIntent);
            }
        });

        postList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                if(i+i1 == i2 && i2 != 0)
                {
                    if(flag_loading == false)
                    {
                        getData("" + url + "?count=" + postListCount + "&after=" + pageAfter);
                    }
                }
            }
        });

    }

    private void getData(String sUrl) {
        JSONObject request = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(GET, sUrl, request, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray data;
                try {
                    data = response.getJSONObject("data").getJSONArray("children");
                    postListCount = response.getJSONObject("data").getInt("dist");
                    pageAfter = response.getJSONObject("data").getString("after");

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonObject = data.getJSONObject(i);
                        String title = jsonObject.getJSONObject("data").getString("title");
                        String author = jsonObject.getJSONObject("data").getString("author");
                        String url = "https://www.reddit.com" + jsonObject.getJSONObject("data").getString("permalink");

                        Post post = new Post(title, author, url);

                        postListArray.add(post);
                        postListAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error " + e, Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "" + error, Toast.LENGTH_SHORT);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public class PostAdapter extends BaseAdapter
    {
        List<Post> posts;
        LayoutInflater inflater;

        public PostAdapter(Context context, List<Post> pList)
        {
            inflater = LayoutInflater.from(context);
            this.posts = pList;
        }

        @Override
        public int getCount()
        {
            return posts.size();
        }

        @Override
        public Object getItem(int position)
        {
            return posts.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        private class ViewHolder
        {
            TextView title;
            TextView author;
        }
        @Override

        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.post_layout, null);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.author = (TextView)convertView.findViewById(R.id.author);
                convertView.setTag(holder);
            }

            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(posts.get(position).getTitle());
            holder.author.setText("Posted by:  " + posts.get(position).getAuthor());

            return convertView;
        }
    }
}
