package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import org.orman.mapper.Model;

import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
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
import es.glasspixel.wlanaudit.activities.KeyListActivity;
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

	protected MenuItem copyMenuItem;

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

		return inflater.inflate(R.layout.saved_keys_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mNetworkListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
			mNetworkListView.setItemsCanFocus(false);
			mNetworkListView
					.setOnItemLongClickListener(new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> parent,
								View view, int position, long id) {
							if (mActionMode != null) {
								return false;
							}
							context_menu_item_position = position;

							// Start the CAB using the ActionMode.Callback
							// defined
							// above
							mActionMode = getSherlockActivity()
									.startActionMode(mActionCallBack);
							mSelectedNetwork = mSavedNetworks.get(position);
							view.setSelected(true);
							mNetworkListView.getCheckedItemPositions().put(0,
									true);
							mActionMode.setTitle(String.valueOf(1)
									+ " "
									+ getResources()
											.getString(
													R.string.context_menu_selected_count_unico));
							mActionMode.getMenu().getItem(0).setVisible(true);

							mNetworkListView
									.setOnItemClickListener(new OnItemClickListener() {

										private SparseBooleanArray checked;

										@Override
										public void onItemClick(
												AdapterView<?> arg0, View arg1,
												int arg2, long arg3) {
											mNetworkListView
													.getCheckedItemPositions()
													.put(0,
															!mNetworkListView
																	.getCheckedItemPositions()
																	.get(arg2));
											int checked_count = 0;
											checked = mNetworkListView
													.getCheckedItemPositions();
											boolean hasCheckedElement = false;
											for (int i = 0; i < checked.size()
													&& !hasCheckedElement; i++) {
												if (checked.get(i)) {
													checked_count++;
												}
												hasCheckedElement = checked
														.valueAt(i);
											}
											arg1.setSelected(checked.get(arg2));
											if (checked_count > 1) {
												mActionMode.getMenu()
														.getItem(0)
														.setVisible(false);
												mActionMode.setTitle(String
														.valueOf(checked_count)
														+ " "
														+ getResources()
																.getString(
																		R.string.context_menu_selected_count_multiple));
											} else if (checked_count == 1) {
												mActionMode.setTitle(String
														.valueOf(checked_count)
														+ " "
														+ getResources()
																.getString(
																		R.string.context_menu_selected_count_unico));
												mActionMode.getMenu()
														.getItem(0)
														.setVisible(true);
											} else {
												mActionMode.finish();
											}

										}
									});
							return true;
						}
					});

		} else {

			mNetworkListView
					.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {

						ArrayList<Network> mCheckedItems;
						android.view.MenuItem copy_item;

						@Override
						public boolean onActionItemClicked(
								android.view.ActionMode mode,
								android.view.MenuItem item) {

							switch (item.getItemId()) {
							case R.id.delete_context_menu:

								for (Network n : mCheckedItems) {
									n.delete();
								}

								mCallback.dataSourceShouldRefresh();
								mode.finish();
								break;
							case R.id.copy_context_menu:
								if (mSelectedNetwork.getPossibleDefaultKeys()
										.size() == 1) {
									copyClipboard(mSelectedNetwork
											.getPossibleDefaultKeys().get(0));
								} else if (mSelectedNetwork
										.getPossibleDefaultKeys().size() > 1) {
									Intent i = new Intent(
											getSherlockActivity(),
											KeyListActivity.class);
									i.putStringArrayListExtra(
											KeyListActivity.KEY_LIST_KEY,
											(ArrayList<String>) mSelectedNetwork
													.getPossibleDefaultKeys());
									startActivity(i);
									getSherlockActivity()
											.overridePendingTransition(
													R.anim.slide_in_from_right,
													R.anim.slide_out_to_left);

								}

								break;
							default:
								break;
							}
							mode.finish();

							return true;
						}

						@Override
						public boolean onCreateActionMode(
								android.view.ActionMode mode,
								android.view.Menu menu) {

							android.view.MenuInflater inflater = mode
									.getMenuInflater();
							inflater.inflate(
									R.menu.saved_keys_elements_context_menu,
									menu);
							copy_item = menu.getItem(0);
							// if (mSelectView == null) {
							// mSelectView = (ViewGroup)
							// LayoutInflater.from(getActivity())
							// .inflate(R.layout.select_count_layout, null);
							//
							// mSelectedCount = (TextView) mSelectView
							// .findViewById(R.id.count_tv);
							//
							// }
							if (mCheckedItems == null) {
								mCheckedItems = new ArrayList<Network>();
							}
							// mode.setCustomView(mSelectView);
							return true;
						}

						@Override
						public void onDestroyActionMode(
								android.view.ActionMode mode) {
							mCheckedItems = null;

						}

						@Override
						public boolean onPrepareActionMode(
								android.view.ActionMode mode,
								android.view.Menu menu) {
							// if (mSelectView == null) {
							// mSelectView = (ViewGroup)
							// LayoutInflater.from(getActivity())
							// .inflate(R.layout.select_count_layout, null);
							//
							// mSelectedCount = (TextView) mSelectView
							// .findViewById(R.id.count_tv);
							// }

							if (mCheckedItems == null) {
								mCheckedItems = new ArrayList<Network>();
							}
							return true;

						}

						@Override
						public void onItemCheckedStateChanged(
								android.view.ActionMode mode, int position,
								long id, boolean checked) {

							final int count = mNetworkListView
									.getCheckedItemCount();
							if (count > 1) {
								copy_item.setVisible(false);
								mode.setTitle(String.valueOf(count)
										+ " "
										+ getResources()
												.getString(
														R.string.context_menu_selected_count_multiple));
							} else {
								mode.setTitle(String.valueOf(count)
										+ " "
										+ getResources()
												.getString(
														R.string.context_menu_selected_count_unico));
								copy_item.setVisible(true);
							}

							// mSelectedCount.setText(String.valueOf(count));
							if (checked) {
								mCheckedItems.add(mSavedNetworks.get(position));
							} else {
								mCheckedItems.remove(mSavedNetworks
										.get(position));
							}

						}

					});
		}

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
			copyMenuItem = menu.getItem(0);
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
				SparseBooleanArray s = mNetworkListView
						.getCheckedItemPositions();
				for (int i = 0; i < s.size(); i++) {
					if (s.get(i)) {
						mSavedNetworks.get(i).delete();
					}
				}

				mCallback.dataSourceShouldRefresh();
				mode.finish();
				return true;
			case R.id.copy_context_menu:
				if (mSelectedNetwork.getPossibleDefaultKeys().size() == 1) {
					copyClipboard(mSelectedNetwork.getPossibleDefaultKeys()
							.get(0));
				} else if (mSelectedNetwork.getPossibleDefaultKeys().size() > 1) {
					Intent i = new Intent(getSherlockActivity(),
							KeyListActivity.class);
					i.putStringArrayListExtra(KeyListActivity.KEY_LIST_KEY,
							(ArrayList<String>) mSelectedNetwork
									.getPossibleDefaultKeys());
					startActivity(i);
					getSherlockActivity().overridePendingTransition(
							R.anim.slide_in_from_right,
							R.anim.slide_out_to_left);

				}

				mode.finish();
				return true;
			default:
				return true;
			}

		}

		public void setCopyMenuItemVisible(boolean visibility) {

		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;

		}
	};

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void copyClipboard(CharSequence text) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData
					.newPlainText("text label", text);
			clipboard.setPrimaryClip(clip);
		} else {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		}
		Toast.makeText(getActivity(),
				getResources().getString(R.string.key_copy_success),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void dataSourceShouldRefresh() {
		mSavedNetworks = Model.fetchAll(Network.class);
		mListAdapter.clear();
		for (Network n : mSavedNetworks) {
			mListAdapter.add(n);
		}

		mListAdapter.notifyDataSetChanged();
	}

}