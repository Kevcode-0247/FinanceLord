package protect.FinanceLord.NetWorthReportEditing.FragmentUtils;

import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.List;

import protect.FinanceLord.NetWorthDataStructure.NodeContainer_Assets;
import protect.FinanceLord.NetWorthDataStructure.TypeTreeProcessor_Assets;

/**
 * A Listener class that will detect whether a parent of the items in an expandable list has been clicked
 */
public class AssetsFragmentChildViewClickListener implements ExpandableListView.OnChildClickListener {

    private TypeTreeProcessor_Assets typeProcessor;
    private List<NodeContainer_Assets> sectionDataSet;
    private int level;

    public AssetsFragmentChildViewClickListener(List<NodeContainer_Assets> sectionDataSet, TypeTreeProcessor_Assets typeProcessor, int level) {
        this.sectionDataSet = sectionDataSet;
        this.typeProcessor = typeProcessor;
        this.level = level;
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        NodeContainer_Assets sectionItem = sectionDataSet.get(i);
        List<NodeContainer_Assets> childSection = typeProcessor.getSubGroup(sectionItem.assetsTypeName, level + 1);

        Log.d("Edit_AFragment", "child Clicked: " + childSection.get(i1).assetsTypeName + ", id in DB: " + childSection.get(i1).assetsTypeId);
        return true;
    }
}