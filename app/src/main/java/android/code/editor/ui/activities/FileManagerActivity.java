/*
 *  This file is part of Android Code Editor.
 *
 *  Android Code Editor is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Android Code Editor is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Android Code Editor.  If not, see <https://www.gnu.org/licenses/>.
 */

package android.code.editor.ui.activities;

import android.code.editor.R;
import android.code.editor.common.utils.FileUtils;
import android.code.editor.ui.adapters.FileList;
import android.code.editor.ui.dialogs.FileCreatorDialog;
import android.code.editor.ui.dialogs.FolderCreatorDialog;
import android.code.editor.ui.dialogs.ProjectCreatorDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileManagerActivity extends BaseActivity {
  private ProgressBar progressbar;
  private RecyclerView list;
  public FileList filelist;
  private String initialDir;
  public String currentDir;

  public ArrayList<String> listString = new ArrayList<>();
  public ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Set Layout in Activity
    setContentView(R.layout.activity_file_manager);
    initialDir = getIntent().getStringExtra("path");
    currentDir = getIntent().getStringExtra("path");
    initActivity();
    loadFileList(initialDir);
  }

  public void initActivity() {
    initViews();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu arg0) {
    super.onCreateOptionsMenu(arg0);
    getMenuInflater().inflate(R.menu.filemanager_activity_menu, arg0);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu arg0) {
    MenuItem item = arg0.findItem(R.id.menu_main_setting);
    Drawable icon =
        getResources()
            .getDrawable(R.drawable.more_vert_fill0_wght400_grad0_opsz48, this.getTheme());
    item.setIcon(icon);
    return super.onPrepareOptionsMenu(arg0);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem arg0) {
    if (arg0.getItemId() == R.id.menu_main_setting) {
      PopupMenu popupMenu =
          new PopupMenu(FileManagerActivity.this, findViewById(R.id.menu_main_setting));
      Menu menu = popupMenu.getMenu();
      menu.add(R.string.new_folder);
      menu.add(R.string.new_file);
      menu.add(R.string.new_project);
      menu.add(R.string.open_source_licenses);
      menu.add(R.string.contributors);
      menu.add(R.string.settings);

      popupMenu.setOnMenuItemClickListener(
          new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
              String title = menuItem.getTitle().toString();
              if (title.equals(getString(R.string.new_file))) {
                new FileCreatorDialog(FileManagerActivity.this);
              } else if (title.equals(getString(R.string.new_folder))) {
                new FolderCreatorDialog(FileManagerActivity.this);
              } else if (title.equals(getString(R.string.new_project))) {
                ProjectCreatorDialog projectDialog =
                    new ProjectCreatorDialog(
                        FileManagerActivity.this,
                        currentDir,
                        new ProjectCreatorDialog.onProjectListUpdate() {
                          @Override
                          public void onRefresh() {
                            listMap.clear();
                            listString.clear();
                            loadFileList(currentDir);
                          }
                        });
                projectDialog.show();
              } else if (title.equals(getString(R.string.contributors))) {
                Intent intent = new Intent();
                intent.setClass(FileManagerActivity.this, ContributorsActivity.class);
                startActivity(intent);
              } else if (title.equals(getString(R.string.settings))) {
                Intent setting = new Intent();
                setting.setClass(FileManagerActivity.this, SettingActivity.class);
                startActivity(setting);
              } else if (title.equals(getString(R.string.open_source_licenses))) {
                Intent license = new Intent();
                license.setClass(FileManagerActivity.this, LicenseActivity.class);
                startActivity(license);
              }
              return true;
            }
          });

      popupMenu.show();
    }
    return super.onOptionsItemSelected(arg0);
  }

  private void initViews() {
    // Setup toolbar
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(
        (view) -> {
          Intent intent = new Intent();
          intent.putExtra("path", currentDir);
          intent.setClass(FileManagerActivity.this, CodeEditorActivity.class);
          startActivity(intent);
        });
    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setTitle(R.string.android_code_editor);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    toolbar.setNavigationOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View arg0) {
            onBackPressed();
          }
        });
    // Define view
    list = findViewById(R.id.list);
    progressbar = findViewById(R.id.progressbar);
  }

  public void loadFileList(String path) {
    listString.clear();
    listMap.clear();
    currentDir = path;
    ExecutorService loadFileList = Executors.newSingleThreadExecutor();
        
    loadFileList.execute(
        new Runnable() {
          @Override
          public void run() {
            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    progressbar.setVisibility(View.VISIBLE);
                  }
                });

            // Get file path from intent and list dir in array
            FileUtils.listDir(path, listString);
            FileUtils.setUpFileList(listMap, listString);

            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    // Set Data in list
                    progressbar.setVisibility(View.GONE);
                    filelist = new FileList(listMap, FileManagerActivity.this);
                    list.setAdapter(filelist);
                    list.setLayoutManager(new LinearLayoutManager(FileManagerActivity.this));
                  }
                });
          }
        });
  }

  @Override
  public void onBackPressed() {
    if (initialDir.equals(currentDir)) {
      finishAffinity();
    } else {
      loadFileList(new File(currentDir).getParent());
    }
  }
}
