package com.svc.sml.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.svc.sml.Activity.OnAccessoryAdapterListener;
import com.svc.sml.Adapter.MixMatchAdapter;
import com.svc.sml.HorizontalListView;
import com.svc.sml.InkarneAppContext;
import com.svc.sml.Model.BaseAccessoryItem;
import com.svc.sml.Model.FaceItem;
import com.svc.sml.R;
import com.svc.sml.Utility.ConstantsUtil;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopMixMatchFragmentH#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopMixMatchFragmentH extends Fragment implements View.OnClickListener {

    private static final String LOGTAG = SpecsHListFragment.class.toString();

    private static final ArrayList<String> listMixMatchFemaleCategory = (ArrayList<String>) Arrays.asList(
            ConstantsUtil.EAccessoryType.eAccTypeBags.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeClutches.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeEarrings.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeSunglasses.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeShoes.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeHair.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeSpecs.toString());
    private static final ArrayList<String> listMixMatchFemaleIcons = (ArrayList<String>) Arrays.asList(
            "btn_mixmatch_option_bags.png",
            "btn_mixmatch_option_clutches.png",
            "btn_mixmatch_option_earrings.png",
            "btn_mixmatch_option_sunglasses.png",
            "btn_mixmatch_option_shoes_female.png",
            "btn_mixmatch_option_hair_female.png",
            "btn_mixmatch_option_specs.png");

    private static final ArrayList<String> listMixMatchMaleCategory = (ArrayList<String>) Arrays.asList(
            ConstantsUtil.EAccessoryType.eAccTypeBags.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeSunglasses.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeShoes.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeHair.toString(),
            ConstantsUtil.EAccessoryType.eAccTypeSpecs.toString());

    private static final ArrayList<String> listMixMatchMaleIcons = (ArrayList<String>) Arrays.asList(
            "btn_mixmatch_option_bags.png",
            "btn_mixmatch_option_sunglasses.png",
            "btn_mixmatch_option_shoes_male.png",
            "btn_mixmatch_option_hair_male.png",
            "btn_mixmatch_option_specs.png");

   private ArrayList<BaseAccessoryItem> arrayAccessories = new ArrayList<BaseAccessoryItem>();
    private MixMatchAdapter adapter;
    private HorizontalListView container_1stLevelSelection;
    private LinearLayout con_mixmatch_category;
    //private ScrollView con_mixmatch_sku_list;
    private LinearLayout con_mixmatch_sku_list;
    private String selectedAccType;
    protected HorizontalListView mHlv;
    protected OnMixMatchFragmentInteractionListener mMixMatchListener;
    private FaceItem faceItem;

    public interface OnMixMatchFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMixMatchFragmentInteraction(BaseAccessoryItem item);
    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShopMixMatchFragmentH() {

    }

    // TODO: Customize parameter initialization
    public static ShopMixMatchFragmentH newInstance(FaceItem faceItem) {
        ShopMixMatchFragmentH fragment = new ShopMixMatchFragmentH();
        Bundle args = new Bundle();
        args.putSerializable("ARG_FACE_OBJ", faceItem);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        String url = ConstantsUtil.URL_BASEPATH + ConstantsUtil.URL_METHOD_SPECSLIST + User.getInstance().getmUserId() + "/" + User.getInstance().getmGender();
//        requestData(url);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_mix_match, container, false);
        mHlv = (HorizontalListView) view.findViewById(R.id.lv_mixmatch);
        view.findViewById(R.id.toolbar_ibtn_looks).setOnClickListener(this);
        view.findViewById(R.id.toolbar_ibtn_redoAvatar).setOnClickListener(this);
        view.findViewById(R.id.toolbar_ibtn_shop).setOnClickListener(this);
        view.findViewById(R.id.toolbar_ibtn_mixmatch).setOnClickListener(this);
        view.findViewById(R.id.toolbar_ibtn_lookalike).setOnClickListener(this);
        con_mixmatch_category = (LinearLayout)view.findViewById(R.id.ll_mixmatch_option_container);
        //con_mixmatch_sku_list = (ScrollView)view.findViewById(R.id.sv_mixmatch_skus_list);
        con_mixmatch_sku_list = (LinearLayout)view.findViewById(R.id.con_mixmatch_listview);
        if (getArguments() != null) {
            faceItem = (FaceItem) getArguments().getSerializable("ARG_FACE_OBJ");
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMixMatchFragmentInteractionListener) {
            mMixMatchListener = (OnMixMatchFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMixMatchListener = null;
    }

    private void setupCustomLists() {
        adapter = new MixMatchAdapter(getActivity(), arrayAccessories, faceItem, new OnAccessoryAdapterListener(){

            @Override
            public void onAccessorySelected(BaseAccessoryItem item) {
                mMixMatchListener.onMixMatchFragmentInteraction(item);
            }

            @Override
            public void onAccessoryClicked(BaseAccessoryItem item) {

            }
        });
        mHlv.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {
        //mHlv.setAdapter(null);
        con_mixmatch_category.setVisibility(View.INVISIBLE);
        con_mixmatch_sku_list.setVisibility(View.VISIBLE);
        switch (v.getId()) {
            case R.id.toolbar_ibtn_looks: {
                selectedAccType = ConstantsUtil.EAccessoryType.eAccTypeBags.toString();
            }
            break;

            case R.id.toolbar_ibtn_redoAvatar: {
                selectedAccType = ConstantsUtil.EAccessoryType.eAccTypeClutches.toString();
            }
            break;

            case R.id.toolbar_ibtn_shop: {
                selectedAccType = ConstantsUtil.EAccessoryType.eAccTypeEarrings.toString();
            }
            break;

            case R.id.toolbar_ibtn_lookalike: {
                selectedAccType = ConstantsUtil.EAccessoryType.eAccTypeShoes.toString();

            }
            break;
            case R.id.toolbar_ibtn_mixmatch: {
                selectedAccType = ConstantsUtil.EAccessoryType.eAccTypeSunglasses.toString();
            }
            break;
            default:
                break;
        }
        InkarneAppContext.getDataSource().getAccessories(selectedAccType);
        arrayAccessories = (ArrayList<BaseAccessoryItem>) InkarneAppContext.getDataSource().getAccessories(selectedAccType);
        setupCustomLists();
    }
}
