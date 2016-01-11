package shishkoam.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ав on 25.12.2015.
 */

public class FileManagerActivity extends AppCompatActivity {
    private AndroidTreeView tView;
    private Context context = this;
    private static HashMap<TreeNode, IconTreeItem> nodes = new  HashMap<TreeNode, IconTreeItem>();
    private static HashMap<TreeNode, Integer> nodesCheckBoxes = new  HashMap<TreeNode, Integer>();
    private static HashMap<TreeNode, TreeNode> parentNodes = new  HashMap<TreeNode, TreeNode>();
    private static HashMap<TreeNode, File> nodesDirs = new  HashMap<TreeNode, File>();
    private static HashMap<TreeNode, Long> nodesSize = new  HashMap<TreeNode, Long>();

    private static TextView statusSizeBar;
    HashMap<String,String> fileList = new HashMap<String,String>();



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        ViewGroup containerView = (ViewGroup) findViewById(R.id.container);

        TreeNode root = TreeNode.root();

        File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Army");
        TreeNode army = getListFiles(directory);
        root.addChild(army);

        statusSizeBar = (TextView) findViewById(R.id.status_size_bar);
        statusSizeBar.setText("Package Size: " + folderSize(directory)/1024 + "  Free Size: " + Utils.megabytesAvailableInExternalStorage() +
                " / " + Utils.TotalExtMemory());

        tView = new AndroidTreeView(this, root);
        tView.setDefaultAnimation(true);
        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);

        containerView.addView(tView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
            }
        }

        initNodesSize();


    }

    public static long getNodeSize(TreeNode node){
        return nodesSize.get(node);
    }

    private void initNodesSize() {
        for(Map.Entry<TreeNode, File> entry : nodesDirs.entrySet()) {
//            setNodeSize(entry.getKey(), entry.getValue());
            setNodeSize(entry.getKey(), folderSize(entry.getValue()));
        }
    }

    private void setNodeSize(TreeNode node, Long size) {
            IconTreeItem item = nodes.get(node);
            if (node.getChildren().size() != 0) {
                item.setSize(size/1028);
                IconTreeItemHolder holder = (IconTreeItemHolder) node.getViewHolder();
                holder.setSizeText(size/1028);
            }
    }

    private TreeNode getListFiles(File parentDir) {
        long length = 0;
        IconTreeItem iconTreeItem = new IconTreeItem( parentDir.getName());
//        iconTreeItem.setFile(parentDir);
        TreeNode mainPackage = new TreeNode(iconTreeItem).setViewHolder(new IconTreeItemHolder(this));
        nodesDirs.put(mainPackage, parentDir);
        nodes.put(mainPackage, iconTreeItem);
        nodesCheckBoxes.put(mainPackage, -1);
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                TreeNode newNode = getListFiles(file);
                mainPackage.addChildren(newNode);
                parentNodes.put(newNode, mainPackage);
                length += nodesSize.get(newNode);
            }
            else length += file.length();
        }
        nodesSize.put(mainPackage, length);
        return mainPackage;
    }

    private static long folderSize(File directory) {
        long length = 0;
        if (directory.exists())
            if (directory.listFiles() != null)
                for (File file : directory.listFiles()) {
                    if (file.isFile())
                        length += file.length();
                    else
                        length += folderSize(file);
                }
        return length;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.expandAll:
                tView.expandAll();
                for(Map.Entry<TreeNode, IconTreeItem> entry : nodes.entrySet()) {
                    setViewOfIconTreeItem(entry.getKey(),true);
                }
                break;

            case R.id.collapseAll:
                tView.collapseAll();
                for(Map.Entry<TreeNode, IconTreeItem> entry : nodes.entrySet()) {
                    setViewOfIconTreeItem(entry.getKey(),false);
                }
                break;
            case R.id.action_refresh:
                Intent intent = new Intent(this, ResponseActivity.class);
                startActivity(intent);
                break;
            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(getResources().getString(R.string.doyouwant));
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCheckedItems();
                        setAllCheckBoxesUnchecked();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                break;
        }
        return true;
    }

    public void changeSizeAfterDeleting(TreeNode node, long size){
        TreeNode parentNode = node.getParent();
        if (parentNode != null && nodesSize.get(parentNode) != null){
            nodesSize.put(parentNode, nodesSize.get(parentNode)-size);
            setNodeSize(parentNode,nodesSize.get(parentNode));
            changeSizeAfterDeleting(parentNode,size);
        }

    }

    private void setAllCheckBoxesUnchecked(){
        for(Map.Entry<TreeNode, Integer> entry : nodesCheckBoxes.entrySet()) {
            if (entry.getValue() == 0) {
                nodesCheckBoxes.put(entry.getKey(),-1);
                changeCheckBox(entry.getKey(),-1);
            }
        }
    }

    public void deleteCheckedItems(){
        ArrayList<TreeNode> nodeForDelete = new ArrayList<>();
        for(Map.Entry<TreeNode, Integer> entry : nodesCheckBoxes.entrySet()) {
            if (entry.getValue() == 1) {
                nodeForDelete.add(entry.getKey());
            }
        }
        for (int i = nodeForDelete.size()-1; i >= 0; i--) {
            TreeNode node = nodeForDelete.get(i);
//                if (nodes.get(node).getFile() != null)
//                    nodes.get(node).getFile().delete();
            //todo move after deleting
            try {
                changeSizeAfterDeleting(node, nodesSize.get(node));
            } catch (NullPointerException e) {}

            deleteRecursive(nodesDirs.get(node));
            deleteRecursiveUiPart(node);
            nodes.remove(node);
            nodesCheckBoxes.remove(node);
            parentNodes.remove(node);

        }
    }

    private void deleteRecursiveUiPart(TreeNode node){
        try {
            List<TreeNode> childrenNodes = node.getChildren();
            if (node.getChildren().size()!=0){
                for (int i = 0; i < childrenNodes.size(); i++) {
                    deleteRecursiveUiPart(childrenNodes.get(i));
                }
            }
            tView.removeNode(node);
        }
        catch (NullPointerException e) {
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        try{
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

            Log.i("Delete", "file " + fileOrDirectory.getPath());
        fileOrDirectory.delete();
        } catch (NullPointerException e){
            if (fileOrDirectory != null && fileOrDirectory.exists())
            fileOrDirectory.delete();
        }

    }

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            changeViewOfIconTreeItem(node);
        }
    };

    private void changeViewOfIconTreeItem(TreeNode node) {
        IconTreeItem item = nodes.get(node);
        if (node.getChildren().size() != 0) {
//            item.setChecked(changeBooleanValue(item.isChecked()));
            item.setOpened(changeBooleanValue(item.isOpened()));
            IconTreeItemHolder holder = (IconTreeItemHolder) node.getViewHolder();
            holder.changeIcons(item.isOpened());
//            nodes.put(node,item);
        }
    }

    public static void setCheckedState(TreeNode treeNode, boolean state){
        int intState;
        if (state) intState = 1;
        else intState = -1;
        nodesCheckBoxes.put(treeNode, intState);
        statusSizeBar.setText("Selected Size:"+getChekedSize()+" " + "  Free Size: " + Utils.megabytesAvailableInExternalStorage() +
                " / " + Utils.TotalExtMemory());
        resetUpperCheckedStates(treeNode);
        resetDownCheckedStates(treeNode, state);
    }

    private static long getChekedSize(){
        long size = 0;
        for(Map.Entry<TreeNode, TreeNode> entry : parentNodes.entrySet()) {
            int counter = 0;

            if (nodesCheckBoxes.get(entry.getKey()) == 1) {
//                for (Map.Entry<TreeNode, TreeNode> entry2 : parentNodes.entrySet()) {
//                    if (entry.getKey() == entry2.getValue()) {
//                        counter++;
//                        break;
//                    }
//                }
//                if (counter == 0)
                if (entry.getKey().getChildren().size() == 0)
                    size += nodesSize.get(entry.getKey());
            }
        }
        return size/1024;
    }

    private static void changeCheckBox(TreeNode treeNode, int intState){
        IconTreeItem item = nodes.get(treeNode);
        if (intState == 1) item.setChecked(true);
        else item.setChecked(false);

        IconTreeItemHolder holder = (IconTreeItemHolder) treeNode.getViewHolder();
        try {
            holder.changeCheckbox(intState);
        }catch (NullPointerException e){
        }
    }

    private static void resetDownCheckedStates(TreeNode treeNode, boolean state){
        int intState;
        if (state) intState = 1;
        else intState = -1;
        for(Map.Entry<TreeNode, TreeNode> entry : parentNodes.entrySet()) {
            if (entry.getValue() == treeNode){
                nodesCheckBoxes.put(entry.getKey(),intState);
                resetDownCheckedStates(entry.getKey(),state);
                changeCheckBox(entry.getKey(), intState);
            }
        }
    }

    private static void resetUpperCheckedStates(TreeNode treeNode){
        try {
            TreeNode parentTreeNode = parentNodes.get(treeNode);
            int intState = getSummedChildrenState(parentTreeNode);
            nodesCheckBoxes.put(parentTreeNode, intState);
            changeCheckBox(parentTreeNode, intState);
            resetUpperCheckedStates(parentTreeNode);
        }catch (NullPointerException e){
        }
    }

    private static int getSummedChildrenState(TreeNode treeNode){
        ArrayList<Integer> childrenStates = new ArrayList<>();
        for(Map.Entry<TreeNode, TreeNode> entry : parentNodes.entrySet()) {
            if (entry.getValue() == treeNode){
                childrenStates.add(nodesCheckBoxes.get(entry.getKey()));
            }
        }
        if (childrenStates.size() > 0) {
            int n = 1;
            int zeroState = childrenStates.get(0);
            for (int i = 1; i < childrenStates.size(); i++) {
                if (childrenStates.get(i) == zeroState)
                    n++;
            }
            if (n == childrenStates.size())
                return zeroState;
            else
                return  0;
        }
        else
            return 1;
    }

    public static int getCheckBoxValue(TreeNode node){
        return nodesCheckBoxes.get(node);
    }

    private void setViewOfIconTreeItem(TreeNode node, boolean open) {
        IconTreeItem item = nodes.get(node);
        if (node.getChildren().size() != 0) {
            item.setOpened(open);
            IconTreeItemHolder holder = (IconTreeItemHolder) node.getViewHolder();
            holder.changeIcons(item.isOpened());
//            nodes.put(node,item);
        }
    }

    private boolean changeBooleanValue(boolean booleanValue){
        if (booleanValue) return false;
        else return true;
    }

    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            IconTreeItem item = (IconTreeItem) value;
            Toast.makeText(context, "Long click: " + item.getText(), Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", tView.getSaveState());
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    public void getFileList() {
        Log.d("FileManagerClient"," FileManagerClient");

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://109.86.155.68:8050/dr/sm/sm/getstructurejson/141341414", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                if (statusCode == HttpURLConnection.HTTP_OK) {
                try {
                    Log.d("FileManagerClient", getString("fileName", response) + " - " + getString("fileSize", response));

                    getListFromJson(response.getJSONArray("inDirectoryList"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                   }
            }
        });
    }
    private void getListFromJson(JSONArray jArr) throws JSONException{
//        JSONArray jArr = jObj.getJSONArray("list");
        for (int i=0;i<jArr.length();i++) {
            JSONObject jListObj = jArr.getJSONObject(i);
            fileList.put(getString("fileName", jListObj),getString("fileSize", jListObj));
            Log.d("FileManagerClient", getString("fileName", jListObj) + " - " + getString("fileSize", jListObj));
//            if (jListObj.getJSONArray("inDirectoryList").length() != 0)
//                getListFromJson(jListObj.getJSONArray("inDirectoryList"));

        }
//        return getString("fileName", jListObj) + getString("fileSize", jListObj);
    }

    private static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float  getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    private static int  getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }
}