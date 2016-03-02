package org.androidcru.crucentralcoast.presentation.views.videos;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.services.youtube.model.SearchResult;
import com.squareup.picasso.Picasso;

import org.androidcru.crucentralcoast.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.CruVideoViewHolder>
{
    private List<SearchResult> videos;
    private LinearLayoutManager layoutManager;

    public VideosAdapter(List<SearchResult> videos, LinearLayoutManager layoutManager)
    {
        this.videos = videos;
        this.layoutManager = layoutManager;
    }

    @Override
    public CruVideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new CruVideoViewHolder(inflater.inflate(R.layout.card_video, parent, false));
    }

    @Override
    public int getItemCount()
    {
        return videos.size();
    }

    @Override
    public void onBindViewHolder(CruVideoViewHolder holder, int position)
    {
        SearchResult searchResult = videos.get(position);
        holder.videoTitle.setText(searchResult.getSnippet().getTitle());
        Context context = holder.videoThumb.getContext();
        holder.vID = searchResult.getId().getVideoId();

        Picasso.with(context)
                .load(searchResult.getSnippet().getThumbnails().getDefault().getUrl())
                .fit()
                .into(holder.videoThumb);



        holder.videoThumb.setOnClickListener((View v) -> {
            //TODO not sure what to do if player cant resolve video
            if(YouTubeIntents.canResolvePlayVideoIntentWithOptions(context))
            {
                context.startActivity(YouTubeIntents.createPlayVideoIntentWithOptions(context, holder.vID, true, true));
            }
        });
    }

    public class CruVideoViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.videoTitle) TextView videoTitle;
        @Bind(R.id.videoThumb) ImageView videoThumb;
        String vID;

        public CruVideoViewHolder(View rootView)
        {
            super(rootView);
            ButterKnife.bind(this, rootView);

        }
    }
}