package de.symeda.sormas.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.symeda.sormas.app.core.IActivityCommunicator;
import de.symeda.sormas.app.core.IDashboardNavigationCapsule;
import de.symeda.sormas.app.core.adapter.multiview.EnumMapDataBinderAdapter;
import de.symeda.sormas.app.core.enumeration.IStatusElaborator;
import de.symeda.sormas.app.util.ConstantHelper;

/**
 * Created by Orson on 08/04/2018.
 * <p>
 * www.technologyboard.org
 * sampson.orson@gmail.com
 * sampson.orson@technologyboard.org
 */
public abstract class BaseSummaryFragment<E extends Enum<E>, TAdapter extends EnumMapDataBinderAdapter<E>> extends BaseFragment {


    private RecyclerView.LayoutManager mLayoutManager;
    private TAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private IActivityCommunicator mActivityCommunicator;
    private TextView mSummarySectionTitle;
    private ProgressBar mPreloader;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(this.getRootListLayout(), container, false);

        mPreloader = (ProgressBar)view.findViewById(R.id.preloader);
        mSummarySectionTitle = (TextView)view.findViewById(R.id.summarySectionTitle);

        mRecyclerView = createRecyclerView(view);
        mLayoutManager = createLayoutManager();
        mAdapter = createSummaryAdapter();

        if (mSummarySectionTitle != null)
            mSummarySectionTitle.setText(getResources().getString(getSectionTitleResId()));


        //view.setMinimumHeight(getResources().getDimensionPixelSize(getMinHeightResId()));

        return view;
    }

    //<editor-fold desc="Public Methods">
    public int getRootListLayout() {
        return R.layout.fragment_root_summary_layout;
    }

    public int getMinHeightResId() {
        return R.dimen.summaryFragmentMinHeight;
    }

    public RecyclerView createRecyclerView(View view) {
        return (RecyclerView) view.findViewById(R.id.recyclerview_main);
    }

    public TAdapter getLandingAdapter() {
        return this.mAdapter;
    }

    public void configure() {
        this.mRecyclerView.setAdapter(this.mAdapter);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
    }

    protected void showPreloader() {
        if (mPreloader != null)
            mPreloader.setVisibility(View.VISIBLE);
    }

    protected void hidePreloader() {
        if (mPreloader != null)
            mPreloader.setVisibility(View.GONE);
    }

    //</editor-fold>

    //<editor-fold desc="Abstract Methods">

    protected abstract int getSectionTitleResId();

    protected abstract TAdapter createSummaryAdapter();

    protected abstract RecyclerView.LayoutManager createLayoutManager();

    protected abstract int getContainerResId();

    //</editor-fold>


    protected void setActivityCommunicator(IActivityCommunicator activityCommunicator) {
        this.mActivityCommunicator = activityCommunicator;
    }

    protected static <TFragment extends BaseSummaryFragment, TCapsule extends IDashboardNavigationCapsule> TFragment newInstance(IActivityCommunicator activityCommunicator, Class<TFragment> f, TCapsule dataCapsule) throws IllegalAccessException, java.lang.InstantiationException {
        TFragment fragment = f.newInstance();

        fragment.setActivityCommunicator(activityCommunicator);

        Bundle bundle = fragment.getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }

        IStatusElaborator pageStatus = dataCapsule.getPageStatus();

        if (pageStatus != null)
            bundle.putSerializable(ConstantHelper.ARG_PAGE_STATUS, pageStatus.getValue());

        fragment.setArguments(bundle);
        return fragment;
    }
}
