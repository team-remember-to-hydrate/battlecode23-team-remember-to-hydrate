Git Workflow Instructions

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