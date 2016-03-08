package org.androidcru.crucentralcoast.presentation.views.resources.articles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.orhanobut.logger.Logger;

import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.Resource;
import org.androidcru.crucentralcoast.data.providers.ResourceProvider;
import org.androidcru.crucentralcoast.presentation.views.ListFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.gujun.android.taggroup.TagGroup;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class ArticlesFragment extends ListFragment
{
    private CustomTabsIntent.Builder customTabsIntentBuilder;
    private AppCompatDialog dialog;
    TagGroup tagGroup;
    Button ok;

    private ArrayList<Resource> resources;
    private Observer<List<Resource>> resourceSubscriber;
    private Subscription subscription;

    public ArticlesFragment()
    {
        resources = new ArrayList<>();
        setupObserver();
    }

    void setupObserver()
    {
        resourceSubscriber = new Observer<List<Resource>>()
        {
            @Override
            public void onCompleted()
            {
                swipeRefreshLayout.setRefreshing(false);

                if (resources.isEmpty())
                    emptyView.setVisibility(View.VISIBLE);
                else
                    emptyView.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable e)
            {
                Logger.e(e, "Resources failed to retrieve.");
            }

            @Override
            public void onNext(List<Resource> resources)
            {
                    setResources(resources);
            }

        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.list_with_empty_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        inflateEmptyView(R.layout.empty_articles_view);
        //parent class calls ButterKnife for view injection and setups SwipeRefreshLayout
        super.onViewCreated(view, savedInstanceState);

        setupDialog();

        //Update the list of resources by pulling from the server
        forceUpdate();

        //LayoutManager for RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        setupCustomTab();

        swipeRefreshLayout.setOnRefreshListener(this::forceUpdate);

        setHasOptionsMenu(true);
    }

    private void setupDialog()
    {
        dialog = new AppCompatDialog(getContext());
        dialog.setContentView(R.layout.filter_dialog);
        ok = (Button) dialog.findViewById(R.id.ok);
        tagGroup = (TagGroup) dialog.findViewById(R.id.tag_group);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filter, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_filter:
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(subscription != null)
            subscription.unsubscribe();
    }

    private void forceUpdate()
    {
        swipeRefreshLayout.setRefreshing(true);
        //Start listening for stream data from network call
        this.resources.clear();
        subscription = ResourceProvider.findResources(new Resource.ResourceType[] { Resource.ResourceType.ARTICLE }, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resourceSubscriber);
    }

    private void setResources(List<Resource> resources)
    {
        //Adapter for RecyclerView
        ArticlesAdapter articlesAdapter = new ArticlesAdapter(getActivity(), new ArrayList<>(resources), customTabsIntentBuilder);
        recyclerView.setAdapter(articlesAdapter);
        this.resources = new ArrayList<>(resources);
    }

    private void setupCustomTab() {
        customTabsIntentBuilder = new CustomTabsIntent.Builder();

        int color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        customTabsIntentBuilder.setToolbarColor(color);

        customTabsIntentBuilder.setShowTitle(true);
    }
}
