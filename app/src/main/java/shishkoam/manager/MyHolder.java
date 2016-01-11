package shishkoam.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

/**
 * Created by ав on 26.12.2015.
 */
public class MyHolder extends TreeNode.BaseNodeViewHolder<IconTreeItem> {
    View parentView;
    Context context;
    public MyHolder(Context context) {
        super(context);
    }
    @Override
    public View createNodeView(TreeNode node, IconTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_profile_node, null, true);
        TextView tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.getText());

        return view;
    }


}