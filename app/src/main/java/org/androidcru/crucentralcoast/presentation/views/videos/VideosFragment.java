package org.androidcru.crucentralcoast.presentation.views.videos;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.orhanobut.logger.Logger;

import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.youtube.Snippet;
import org.androidcru.crucentralcoast.data.providers.YouTubeVideoProvider;
import org.androidcru.crucentralcoast.presentation.views.base.BaseSupportFragment;
import org.androidcru.crucentralcoast.util.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import timber.log.Timber;

public class VideosFragment extends BaseSupportFragment
{
    @BindView(R.id.video_list) RecyclerView videoList;
    @BindView(R.id.video_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.empty_videos_view) RelativeLayout emptyVideoLayout;

    private LinearLayoutManager layoutManager;
    private Observer<List<Snippet>> videoSubscriber;
    private List<Snippet> videos;
    // used for filtering duplicate videos before being passed to the adapter
    private List<Snippet> tempVideos;
    private YouTubeVideoProvider youtubeProvider;
    private VideosAdapter videosAdapter;
    private int curSize;
    private boolean searchEnabled;
    private String searchQuery;

    public VideosFragment()
    {
        curSize = 0;
        videos = new ArrayList<>();
        tempVideos = new ArrayList<>();
        youtubeProvider = new YouTubeVideoProvider();

        // Display text notifying the user if there are no videos to load, else show the videos
        videoSubscriber = new Observer<List<Snippet>>()
        {
            @Override
            public void onCompleted() {
                if(videos.isEmpty())
                {
                    swipeRefreshLayout.setVisibility(View.GONE);
                    emptyVideoLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    emptyVideoLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Throwable e)
            {
                Timber.e(e, "videos failed to retrieve.");
            }

            @Override
            public void onNext(List<Snippet> searchResults)
            {
                setVideos(searchResults);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_videos, container, false);
    }

    // Inflate and set the query listener for the search bar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.youtube, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                searchVideos(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchEnabled = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchEnabled = false;
                forceUpdate();
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unbinder = ButterKnife.bind(this, view);

        setHasOptionsMenu(true);
        layoutManager = new LinearLayoutManager(getActivity());
        videoList.setLayoutManager(layoutManager);

        // Set the Recycler View to scroll so long as there are more videos that
        // can be returned by the provider.
        videoList.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount)
            {
                if(searchEnabled)
                    youtubeProvider.requestVideoSearch(VideosFragment.this, videoSubscriber, searchQuery);
                else
                    getCruVideos();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.cruDarkBlue, R.color.cruGold, R.color.cruOrange);
        swipeRefreshLayout.setOnRefreshListener(this::forceUpdate);
    }

    private void getCruVideos()
    {
        youtubeProvider.requestChannelVideos(this, videoSubscriber);
    }

    // Places the videos in the returned response into the Adapter's list of videos
    public void setVideos (List<Snippet> newVideos)
    {
        tempVideos.addAll(newVideos);

        // Only set the Adapter once - on the first video request
        // Otherwise, the Adapter resets the scroll progression to the top of the list
        if(videosAdapter == null)
        {
            videosAdapter = new VideosAdapter(videos, layoutManager);
            videoList.setAdapter(videosAdapter);
            videos.addAll(tempVideos);
            curSize += tempVideos.size();
        }
        else
        {
            // Don't add the video if it is already in the videos list
            curSize += addIfNotDuplicated(videos, tempVideos);

            // Let the Adapter know that more videos have been added to its list.
            videosAdapter.notifyItemChanged(curSize, videosAdapter.getItemCount() - 1);
        }
        videosAdapter.updateViewExpandedStates();

        // Used for keeping track of the user's scroll progression through the list of videos.
        curSize += newVideos.size();
        swipeRefreshLayout.setRefreshing(false);
        tempVideos.clear();
    }

    // Takes in 2 lists and appends the non-duplicated contents of the new list to the old list
    // Returns the number of new items added to the new list
    public static <T> int addIfNotDuplicated(List<T> old, List<T> newItems)
    {
        int count = 0;
        Iterator<T> iterator = newItems.iterator();
        while (iterator.hasNext())
        {
            T sr = iterator.next();
            if(!old.contains(sr))
            {
                old.add(sr);
                ++count;
            }
        }

        return count;
    }

    // Search the youtube channel for a specific video
    void searchVideos(String query) {
        videos.clear();
        curSize = 0;
        videosAdapter = null;
        youtubeProvider.resetQuery();
        youtubeProvider.requestVideoSearch(this, videoSubscriber, query);
    }

    private void forceUpdate()
    {
        // Reset the Adapter and video-related isExpanded
        videos.clear();
        curSize = 0;
        videosAdapter = null;

        youtubeProvider.resetQuery();
        if(searchEnabled)
            youtubeProvider.requestVideoSearch(this, videoSubscriber, searchQuery);
        else
            youtubeProvider.requestChannelVideos(this, videoSubscriber);

    }

    @Override
    public void onResume()
    {
        // TODO this sets the user back at the top of the list. Should resume at the position of where the user left the activity.
        super.onResume();
        getCruVideos();
    }
}