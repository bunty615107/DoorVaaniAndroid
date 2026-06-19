# F-Droid Submission Guide for DoorVaani Android

This guide outlines the step-by-step process for submitting the **DoorVaani Android** app to the F-Droid repository.

## Prerequisites

1.  **GitLab Account:** You need an account on [GitLab](https://gitlab.com/) as F-Droid uses GitLab for managing submissions via merge requests.
2.  **F-Droid Data Fork:** Fork the [fdroiddata repository](https://gitlab.com/fdroid/fdroiddata) to your GitLab account.
3.  **Local Repository Setup:** Clone your fork locally to prepare the submission.
    ```bash
    git clone https://gitlab.com/<your-username>/fdroiddata.git
    cd fdroiddata
    ```

## Step 1: Add Metadata File

1.  In your local `fdroiddata` repository, navigate to the `metadata/` directory.
2.  Copy the `com.doorvaani.android.yml` file from the DoorVaani Android repository's `fdroid/` folder into this `metadata/` directory.
    ```bash
    cp /path/to/DoorVaaniAndroid/fdroid/com.doorvaani.android.yml metadata/
    ```

## Step 2: Validate the Metadata File

Before committing, it's essential to validate the metadata file using the `fdroid` server tools to ensure it meets F-Droid's formatting and requirement standards.

1.  Install fdroidserver (if not already installed). Refer to the [F-Droid documentation](https://f-droid.org/docs/Installing_the_Server_and_Repo_Tools/) for instructions.
2.  Run the linter:
    ```bash
    fdroid lint com.doorvaani.android
    ```
3.  Fix any warnings or errors reported by the linter in your `com.doorvaani.android.yml` file.

## Step 3: Run a Local Build Test (Optional but Recommended)

To ensure F-Droid's build server will successfully build the app:
```bash
fdroid build -v -l com.doorvaani.android
```
Make sure the build completes successfully.

## Step 4: Commit and Push Changes

1.  Create a new branch for your submission.
    ```bash
    git checkout -b add-doorvaani
    ```
2.  Stage the new metadata file.
    ```bash
    git add metadata/com.doorvaani.android.yml
    ```
3.  Commit the changes with a clear message.
    ```bash
    git commit -m "Add DoorVaani Android (com.doorvaani.android)"
    ```
4.  Push the branch to your fork on GitLab.
    ```bash
    git push origin add-doorvaani
    ```

## Step 5: Create a Merge Request

1.  Go to your fork on GitLab in your web browser.
2.  You should see a prompt to "Create merge request" for your recently pushed branch. Click it.
3.  Set the target branch to `fdroiddata/master`.
4.  Fill out the Merge Request template provided by F-Droid. Ensure you check all the relevant boxes indicating that the app is open-source, uses an accepted license, and doesn't have proprietary dependencies.
5.  Submit the Merge Request.

## Step 6: Address Feedback

F-Droid maintainers and bots will review the Merge Request.
- **Bot Checks:** The `fdroid-bot` will automatically run checks. If it reports issues, fix them in your branch and push the changes; the MR will update automatically.
- **Human Review:** Maintainers may leave comments or ask questions. Respond promptly and make necessary adjustments to your metadata or source code (if changes are needed to comply with F-Droid's inclusion policy).

Once approved, your app will be merged and eventually built and published on the F-Droid store!

## Additional Resources
- [F-Droid Inclusion Policy](https://f-droid.org/docs/Inclusion_Policy/)
- [F-Droid Metadata Reference](https://f-droid.org/docs/Build_Metadata_Reference/)
