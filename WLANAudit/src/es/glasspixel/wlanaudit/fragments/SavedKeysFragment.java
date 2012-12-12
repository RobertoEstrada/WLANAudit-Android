package es.glasspixel.wlanaudit.fragments;

import java.util.List;

import org.orman.mapper.Model;

import roboguice.inject.InjectView;
import sun.security.action.GetIntegerAction;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.AboutActivity;
import es.glasspixel.wlanaudit.activities.SlidingMapActivity;
import es.glasspixel.wlanaudit.activities.WLANAuditPreferencesActivity;
import es.glasspixel.wlanaudit.adapters.SavedNetworksAdapter;
import es.glasspixel.wlanaudit.database.entities.Network;
import es.glasspixel.wlanaudit.interfaces.OnDataSourceModifiedListener;

public class SavedKeysFragment extends RoboSherlockListFragment implements
		OnDataSourceModifiedListener {

	/**
	 * Interface to pass fragment callbacks to parent activity. Parent activity
	 * must implement this to be aware of the events of the fragment.
	 */
	public interface SavedNetworkFragmentListener extends
			OnDataSourceModifiedListener {
		/**
		 * Observers must implement this method to be notified of which network
		 * was selected on this fragment.
		 * 
		 * @param networkData
		 *            The network data of the selected item.
		 */
		public void onNetworkSelected(Network networkData);

		/**
		 * Fragments which use a shared datasource with other fragments, should
		 * notify them using this method.
		 */
		public void dataSourceShouldRefresh();
	}

	protected ActionMode mActionMode;

	protected int context_menu_item_position;

	private List<Network> mSavedNetworks;

	protected Network mSelectedNetwork;

	private SavedNetworkFragmentListener mCallback;

	@InjectView(android.R.id.list)
	private ListView mNetworkListView;

	private SavedNetworksAdapter mListAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (SavedNetworkFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnDataSourceModifiedListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Cargamos datos desde la BD
		mSavedNetworks = Model.fetchAll(Network.class);
		// Creamos el adapter
		mListAdapter = new SavedNetworksAdapter(getSherlockActivity(),
				R.layout.key_saved_list_element, mSavedNetworks);
		// Conectamos el adapter a la lista
		setListAdapter(mListAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		boolean b = getArguments().getBoolean("screen_large");

		// if (!b)
		// setHasOptionsMenu(true);

		return inflater.inflate(R.layout.saved_keys_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mNetworkListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mNetworkListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						if (mActionMode != null) {
							return false;
						}
						context_menu_item_position = position;

						// Start the CAB using the ActionMode.Callback defined
						// above
						mActionMode = getSherlockActivity().startActionMode(
								mActionCallBack);
						mSelectedNetwork = mSavedNetworks.get(position);
						view.setSelected(true);
						return true;
					}
				});

		mNetworkListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCallback.onNetworkSelected((Network) parent
						.getItemAtPosition(position));
			}
		});
	}

	private ActionMode.Callback mActionCallBack = new Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.saved_keys_elements_context_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.delete_context_menu:
				mSelectedNetwork.delete();
				mCallback.dataSourceShouldRefresh();
				mode.finish();
				return true;
			case R.id.copy_context_menu:
				// TODO
				/*
				 * copyClipboard(mSelectedNetwork.getKey()); saveWLANKey(
				 * ((TextView) myFragmentView.findViewById(R.id.networkName))
				 * .getText().toString(), mSelectedNetwork.getKey());
				 */
				mode.finish();
				return true;
			default:
				return true;
			}

		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;

		}
	};

	@Override
	public void dataSourceShouldRefresh() {
		mSavedNetworks = Model.fetchAll(Network.class);
		mListAdapter.clear();
		mListAdapter.addAll(mSavedNetworks);
		mListAdapter.notifyDataSetChanged();
	}
}