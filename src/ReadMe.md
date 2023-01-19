Git Workflow Instructions

Current Workflow:
1. Code features into the 'latest' package as normal (usually start with a branch from main).
2. Test feature (ensure it compiles, run against other bot versions)
3. Make a new package *named after feature branch* and copy 'latest' (with your changes) into it. Possibly use intellij refactoring tools to assist.
4. Continue to make PR (merge any new changes on main into feature branch, test again).


Here's the relevant excerpt from our Discord thread.
The main goals are:
1. Occasionally archive current bot (latest) status for later comparison.
2. Still be able to use a single package, 'latest', to allow git to work its magic for us in terms of nicely knitting our code together.

I believe we can accomplish this by coding in the 'latest' package on your feature branch as one normally would, and then when you are happy with your finished feature (and tested it), make a new package named after the feature branch name and copy 'latest' into it.
Then continue to make a PR. The step of making the feature_branch_package will leave a bot version for the next person to test against. You will have been testing against the most recent previous_persons_feature_branch_package that you obtained when you last merged main into your feature branch.

I think this is more straightforward that what we came up with last night. One thing to watch out for is path references in the feature_branch_package (use intellij's refactoring to make this go smoothly, or find/replace any referencing issues that arise in the feature package).


OLD WORKFLOW
1. Make feature branch
2. Make Package named after feature branch
3. Copy latest into new package (named after branch)
4. Make desired feature changes in feature package
5. Commit locally, checkout main, git pull, checkout feature branch, merge main into feature branch, then do testing locally (make sure bots work as expected, changes are beneficial).
6. Copy feature package contents into latest package (overwriting local latest package on feature branch)
7. checkout main and pull new changes (if any PRs were approved)
8. checkout feature branch and merge main (this updates 'latest' with our new features to have any new code merged in)
9. Test new latest that contains both our feature branch feature and new changes to official latest
10. Open PR