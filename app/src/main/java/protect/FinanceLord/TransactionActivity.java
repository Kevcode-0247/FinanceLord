package protect.FinanceLord;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import protect.FinanceLord.Communicators.GroupByCategoryCommunicator;
import protect.FinanceLord.Database.BudgetsType;
import protect.FinanceLord.Database.BudgetsTypeDao;
import protect.FinanceLord.Database.FinanceLordDatabase;
import protect.FinanceLord.Database.Transactions;
import protect.FinanceLord.Database.TransactionsDao;
import protect.FinanceLord.TransactionEditingUtils.BudgetTypesDataModel;
import protect.FinanceLord.TransactionViewingUtils.CategoryLabelsAdapter;
import protect.FinanceLord.TransactionViewingUtils.ViewPagerAdapter;
import protect.FinanceLord.TransactionViewingUtils.View_TransactionsFragment;
import protect.FinanceLord.ViewModels.BudgetTypesViewModel;
import protect.FinanceLord.ViewModels.TransactionsViewModel;

public class TransactionActivity extends AppCompatActivity {

    private TransactionsViewModel viewModel;
    private List<BudgetsType> budgetsTypes;
    private static String TAG = "TransactionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        viewModel = ViewModelProviders.of(TransactionActivity.this).get(TransactionsViewModel.class);
        ImageButton returnButton = findViewById(R.id.transaction_return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        retrieveDataFromDatabase(true, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveDataFromDatabase(false, true);
    }

    private void retrieveDataFromDatabase(final boolean initialize, final boolean refresh) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                FinanceLordDatabase database = FinanceLordDatabase.getInstance(TransactionActivity.this);
                TransactionsDao transactionsDao = database.transactionsDao();
                BudgetsTypeDao budgetsTypeDao = database.budgetsTypeDao();
                final List<Transactions> transactions = transactionsDao.queryAllTransaction();
                budgetsTypes = budgetsTypeDao.queryAllBudgetsTypes();

                for (Transactions transaction : transactions) {
                    Log.d(TAG + " retrieveData", "this item is " + transaction.getTransactionName() + " value is " + transaction.getTransactionValue());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (initialize){
                            setUpCategoryViewList();
                            setUpTabsAndAddButton(transactions);
                        } else if (refresh){
                            viewModel.pushToTransactionGroup(transactions);
                        }
                    }
                });
            }
        });
    }

    private void setUpTabsAndAddButton(List<Transactions> transactions) {
        ImageButton addButton = findViewById(R.id.add_transaction_button);
        TabLayout tablayout = findViewById(R.id.transaction_tab_layout);
        final ViewPager viewPager = findViewById(R.id.transaction_view_pager);

        ArrayList<BudgetTypesDataModel> dataModels = new ArrayList<>();
        if (budgetsTypes != null){
            for (BudgetsType budgetsType : budgetsTypes) {
                BudgetTypesDataModel dataModel = new BudgetTypesDataModel(budgetsType.getBudgetsCategoryId(), budgetsType.getBudgetsName());
                dataModels.add(dataModel);
            }
        } else {
            dataModels = null;
        }

        for (Transactions transaction : transactions) {
            Log.d(TAG + " setUpTabsAndAddButton", "this item is " + transaction.getTransactionName() + " value is " + transaction.getTransactionValue());
        }

        ArrayList<Fragment> fragments = new ArrayList<>();
        View_TransactionsFragment revenuesFragment = new View_TransactionsFragment(transactions, dataModels, getString(R.string.revenues_fragment_key));
        View_TransactionsFragment expensesFragment = new View_TransactionsFragment(transactions, dataModels, getString(R.string.expenses_fragments_key));
        fragments.add(revenuesFragment);
        fragments.add(expensesFragment);

        ViewPagerAdapter sectionsPagerAdapter = new ViewPagerAdapter(this, getSupportFragmentManager(), fragments);
        viewPager.setAdapter(sectionsPagerAdapter);
        tablayout.setupWithViewPager(viewPager);

        final ArrayList<BudgetTypesDataModel> finalDataModels = dataModels;
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(getString(R.string.budget_categories_key), finalDataModels);
                intent.setClass(TransactionActivity.this, TransactionAddActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setUpCategoryViewList() {
        addLabelAll(budgetsTypes);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView categoryLabelsList = findViewById(R.id.transaction_label_list);
        categoryLabelsList.setLayoutManager(layoutManager);
        final CategoryLabelsAdapter adapter = new CategoryLabelsAdapter(this, budgetsTypes, fromAdapterCommunicator);
        categoryLabelsList.setAdapter(adapter);

        BudgetTypesViewModel viewModel = ViewModelProviders.of(this).get(BudgetTypesViewModel.class);
        viewModel.getCategoryLabels().observe(this, new Observer<List<BudgetsType>>() {
            @Override
            public void onChanged(List<BudgetsType> newBudgetsTypesList) {
                budgetsTypes = newBudgetsTypesList;
                addLabelAll(budgetsTypes);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void addLabelAll(List<BudgetsType> budgetsTypes) {
        BudgetsType allBudgets = new BudgetsType();
        allBudgets.setBudgetsName(getString(R.string.all));
        budgetsTypes.add(0, allBudgets);
    }

    private GroupByCategoryCommunicator fromAdapterCommunicator = new GroupByCategoryCommunicator() {
        @Override
        public void onActivityMessage(final String categoryLabel) {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    FinanceLordDatabase database = FinanceLordDatabase.getInstance(TransactionActivity.this);
                    TransactionsDao transactionsDao = database.transactionsDao();
                    BudgetsTypeDao budgetsTypeDao = database.budgetsTypeDao();
                    final List<BudgetsType> budgetsTypes = budgetsTypeDao.queryAllBudgetsTypes();

                    List<Transactions> groupedTransactions = new ArrayList<>();
                    for (BudgetsType budgetsType : budgetsTypes) {
                        if (budgetsType.getBudgetsName().equals(categoryLabel)) {
                            groupedTransactions = transactionsDao.queryTransactionByCategoryId(budgetsType.getBudgetsCategoryId());
                            Log.d(TAG,"budget type name is " + budgetsType.getBudgetsName());
                        }
                    }

                    if (getString(R.string.all).equals(categoryLabel)){
                        groupedTransactions = transactionsDao.queryAllTransaction();
                    }

                    final List<Transactions> finalGroupedTransactions = groupedTransactions;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewModel.pushToTransactionGroup(finalGroupedTransactions);
                        }
                    });
                }
            });
        }
    };
}
