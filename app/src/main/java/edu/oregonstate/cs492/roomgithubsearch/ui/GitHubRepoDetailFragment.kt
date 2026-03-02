package edu.oregonstate.cs492.roomgithubsearch.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import edu.oregonstate.cs492.roomgithubsearch.R
import androidx.core.net.toUri
import androidx.fragment.app.viewModels

class GitHubRepoDetailFragment : Fragment(R.layout.fragment_github_repo_detail) {
    private val viewModel: BookmarkedReposViewModel by viewModels()
    private val args: GitHubRepoDetailFragmentArgs by navArgs()
    private var isBookmarked = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repoNameTV: TextView = view.findViewById(R.id.tv_repo_name)
        val repoStarsTV: TextView = view.findViewById(R.id.tv_repo_stars)
        val repoDescriptionTV: TextView = view.findViewById(R.id.tv_repo_description)

        repoNameTV.text = args.repo.name
        repoStarsTV.text = args.repo.stars.toString()
        repoDescriptionTV.text = args.repo.description

        var bookmarkMenuItem: MenuItem? = null
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.github_repo_detail_menu, menu)
                    bookmarkMenuItem = menu.findItem(R.id.action_bookmark)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_open_in_browser -> {
                            viewOnGitHub()
                            true
                        }
                        R.id.action_share -> {
                            share()
                            true
                        }
                        R.id.action_bookmark -> {
                            toggleRepoBookmark(menuItem)
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.STARTED
        )

        viewModel.getBookmarkedRepoByName(args.repo.name).observe(viewLifecycleOwner) { bookmarkedRepo ->
            when (bookmarkedRepo) {
                null -> {
                    isBookmarked = false
                    bookmarkMenuItem?.isChecked = false
                    bookmarkMenuItem?.icon = AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.ic_action_bookmark_off
                    )
                }
                else -> {
                    isBookmarked = true
                    bookmarkMenuItem?.isChecked = true
                    bookmarkMenuItem?.icon = AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.ic_action_bookmark_on
                    )
                }
            }
        }
    }

    /**
     * This method toggles the state of the bookmark icon in the top app bar whenever the user
     * clicks it.
     */
    private fun toggleRepoBookmark(menuItem: MenuItem) {
        when (!isBookmarked) {
            true -> {
                viewModel.addBookmarkedRepo(args.repo)
            }
            false -> {
                viewModel.removeBookmarkedRepo(args.repo)
            }
        }
    }

    private fun viewOnGitHub() {
        val uri = args.repo.url.toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(
                requireView(),
                R.string.open_in_browser_error,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun share() {
        val shareText = getString(R.string.share_text, args.repo.name, args.repo.url)
        val intent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))
    }
}