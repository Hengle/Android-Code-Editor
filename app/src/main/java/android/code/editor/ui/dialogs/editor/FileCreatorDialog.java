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

package android.code.editor.ui.dialogs.editor;

import android.code.editor.R;
import android.code.editor.interfaces.PathCreationListener;
import android.code.editor.ui.activities.CodeEditorActivity;
import android.code.editor.common.utils.FileUtils;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;

public class FileCreatorDialog {
  public FileCreatorDialog(CodeEditorActivity activity, File dir, PathCreationListener listener) {
    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(activity);
    dialog.setTitle(R.string.create_new_file);
    dialog.setMessage(R.string.to_create_file_enter_name);
    ViewGroup nameCont =
        (LinearLayout)
            activity
                .getLayoutInflater()
                .inflate(android.code.editor.R.layout.layout_edittext_dialog, null);
    EditText path = nameCont.findViewById(android.code.editor.R.id.edittext1);
    TextInputLayout textInputLayout =
        nameCont.findViewById(android.code.editor.R.id.TextInputLayout1);
    textInputLayout.setHint(R.string.enter_file_name);
    path.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void afterTextChanged(Editable arg0) {}

          @Override
          public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            if (path.getText().length() == 0) {
              path.setError(activity.getString(R.string.please_enter_a_file_name));
            } else if (path.getText()
                .toString()
                .substring(path.getText().toString().length() - 1)
                .equals(".")) {
              path.setError(activity.getString(R.string.please_enter_a_valid_name));
            } else if (new File(
                    dir.getAbsolutePath().concat(File.separator).concat(path.getText().toString()))
                .exists()) {
              path.setError(
                  activity.getString(R.string.please_enter_a_file_name_that_does_not_exists));
            } else {
              path.setError(null);
            }
          }

          @Override
          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });
    dialog.setView(nameCont);
    dialog.setPositiveButton(
        R.string.create,
        (param0, param1) -> {
          if (path.getText().length() == 0) {
            Toast.makeText(activity, R.string.please_enter_a_file_name, Toast.LENGTH_SHORT).show();
          } else if (path.getText()
              .toString()
              .substring(path.getText().toString().length() - 1)
              .equals(".")) {
            Toast.makeText(activity, R.string.please_enter_a_valid_name, Toast.LENGTH_SHORT).show();
          } else if (new File(
                  dir.getAbsolutePath().concat(File.separator).concat(path.getText().toString()))
              .exists()) {
            Toast.makeText(
                    activity,
                    R.string.please_enter_a_file_name_that_does_not_exists,
                    Toast.LENGTH_SHORT)
                .show();
          } else if (!new File(
                  dir.getAbsolutePath().concat(File.separator).concat(path.getText().toString()))
              .exists()) {
            FileUtils.writeFile(
                dir.getAbsolutePath().concat(File.separator).concat(path.getText().toString()),
                getFileTemplate(
                    new File(
                        dir.getAbsolutePath()
                            .concat(File.separator)
                            .concat(path.getText().toString())),
                    activity));
            if (listener != null) {
              listener.onPathCreated(
                  new File(
                      dir.getAbsolutePath()
                          .concat(File.separator)
                          .concat(path.getText().toString())));
            }
          }
        });
    dialog.setNegativeButton(
        R.string.cancel,
        (param0, param1) -> {
          dialog.create().dismiss();
        });
    dialog.create().show();
  }

  public static String getFileTemplate(File path, Context context) {
    String content = "";
    String pathToCopyText = "";
    switch (FileUtils.getPathFormat(path.getAbsolutePath())) {
      case "html":
        pathToCopyText = "Templates/NewFiles/template_01.html";
        content = FileUtils.readFileFromAssets(context.getAssets(), pathToCopyText);
        content =
            content.replace(
                "${Project_Name}",
                FileUtils.getLatSegmentOfFilePath(path.getParentFile().getAbsolutePath()));
        break;
      case "css":
        pathToCopyText = "Templates/NewFiles/template_02.css";
        content = FileUtils.readFileFromAssets(context.getAssets(), pathToCopyText);
        break;
      case "js":
        pathToCopyText = "Templates/NewFiles/template_03.js";
        content = FileUtils.readFileFromAssets(context.getAssets(), pathToCopyText);
        break;
      case "java":
        pathToCopyText = "Templates/NewFiles/template_04.java";
        content = FileUtils.readFileFromAssets(context.getAssets(), pathToCopyText);
        content =
            content.replace(
                "${Class_Name}",
                FileUtils.getLatSegmentOfFilePath(path.getAbsolutePath())
                    .substring(
                        0, FileUtils.getLatSegmentOfFilePath(path.getAbsolutePath()).length() - 5));
        break;
    }
    return content;
  }
}
