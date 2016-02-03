package org.androidcru.crucentralcoast.presentation.views.fragments.passengersignup;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.presentation.util.NonSwipeableViewPager;
import org.androidcru.crucentralcoast.presentation.views.activities.forms.FormHolder;
import org.androidcru.crucentralcoast.presentation.views.activities.forms.FormPage;
import org.androidcru.crucentralcoast.presentation.views.adapters.PassengerPagerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PassengerFragment extends Fragment implements FormPage
{
    @Bind(R.id.viewPager) NonSwipeableViewPager viewPager;

    private FormHolder formHolder;
    private PassengerPagerAdapter passengerPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.form, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        passengerPagerAdapter = new PassengerPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(passengerPagerAdapter);
        formHolder.setTitle("Passenger Information");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        passengerPagerAdapter.getRegisteredFragment(viewPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onNext() {
        if(viewPager.getCurrentItem() + 1 >= passengerPagerAdapter.getCount() - 1)
        {
            formHolder.setToolbarExpansion(false);
        }
        else
        {
            formHolder.setToolbarExpansion(true);
        }
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    @Override
    public void onPrevious() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        formHolder.setToolbarExpansion(true);
    }

    @Override
    public void setFormHolder(FormHolder holder) {
        this.formHolder = holder;
    }
}

