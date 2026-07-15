package com.adentweets.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adentweets.app.data.model.Post
import com.adentweets.app.data.model.PostWithAuthor
import com.adentweets.app.data.model.User
import com.adentweets.app.ui.theme.*
import com.adentweets.app.util.toRelativeTime
import com.adentweets.app.util.mentions
import com.adentweets.app.util.hashtags

@Composable
fun TweetCard(
    postWithAuthor: PostWithAuthor,
    onLike: (String) -> Unit,
    onRepost: (String) -> Unit,
    onReply: (String) -> Unit,
    onBookmark: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onTweetClick: (String) -> Unit,
    onMediaClick: (List<String>, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    isDetail: Boolean = false
) {
    val post = postWithAuthor.post
    val author = postWithAuthor.author
    val likeColor by animateColorAsState(
        targetValue = if (postWithAuthor.isLikedByCurrentUser) XRed.value else MaterialTheme.colorScheme.onSurface,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "likeColor"
    )
    val repostColor by animateColorAsState(
        targetValue = if (postWithAuthor.isRepostedByCurrentUser) XGreen.value else MaterialTheme.colorScheme.onSurface,
        label = "repostColor"
    )
    val bookmarkColor by animateColorAsState(
        targetValue = if (postWithAuthor.isBookmarkedByCurrentUser) XBlue.value else MaterialTheme.colorScheme.onSurface,
        label = "bookmarkColor"
    )
    val scale = remember { Animatable(1f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTweetClick(post.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (post.isRepost && !post.repostAuthorUsername.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp, start = 32.dp)
            ) {
                Icon(
                    Icons.Default.Repeat, contentDescription = null,
                    modifier = Modifier.size(16.dp), tint = XGray.value
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${post.repostAuthorName} reposted",
                    style = MaterialTheme.typography.bodySmall,
                    color = XGray.value
                )
            }
        }
        if (post.replyToId != null && !isDetail) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp, start = 32.dp)
            ) {
                Text(
                    text = "Replying to...",
                    style = MaterialTheme.typography.bodySmall,
                    color = XBlue.value
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    ProfileAvatar(
                        avatarUrl = author.avatarUrl,
                        name = author.name,
                        size = 44.dp,
                        modifier = Modifier.clickable { onProfileClick(author.id) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = author.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (author.isVerified) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    modifier = Modifier.size(18.dp).padding(start = 4.dp),
                                    tint = XBlue.value
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "@${author.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = XGray.value,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "· ${post.createdAt.toRelativeTime()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = XGray.value
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { }, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    Icons.Default.MoreHoriz,
                                    contentDescription = "More",
                                    modifier = Modifier.size(18.dp),
                                    tint = XGray.value
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        if (post.repostOfId != null && post.repostAuthorName != null) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = XGray.value, fontSize = 13.sp)) {
                                        append("Reposting ")
                                    }
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp)) {
                                        append("@${post.repostAuthorUsername}")
                                    }
                                },
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        Text(
                            text = post.text.highlightMentionsAndHashtags(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (isDetail) Int.MAX_VALUE else 6,
                            overflow = if (isDetail) TextOverflow.Visible else TextOverflow.Ellipsis
                        )
                        if (post.mediaUrls.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            MediaGrid(
                                mediaUrls = post.mediaUrls,
                                mediaType = post.mediaType,
                                onMediaClick = onMediaClick
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        if (!isDetail) {
                            ActionBar(
                                replyCount = post.replyCount,
                                repostCount = post.repostCount,
                                likeCount = post.likeCount,
                                isLiked = postWithAuthor.isLikedByCurrentUser,
                                isReposted = postWithAuthor.isRepostedByCurrentUser,
                                isBookmarked = postWithAuthor.isBookmarkedByCurrentUser,
                                likeColor = likeColor,
                                repostColor = repostColor,
                                bookmarkColor = bookmarkColor,
                                onReply = { onReply(post.id) },
                                onRepost = { onRepost(post.id) },
                                onLike = {
                                    scale.animateTo(1.3f, spring(stiffness = Spring.StiffnessHigh))
                                    scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    onLike(post.id)
                                },
                                onBookmark = { onBookmark(post.id) }
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp).offset(x = 56.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun ActionBar(
    replyCount: Long,
    repostCount: Long,
    likeCount: Long,
    isLiked: Boolean,
    isReposted: Boolean,
    isBookmarked: Boolean,
    likeColor: Color,
    repostColor: Color,
    bookmarkColor: Color,
    onReply: () -> Unit,
    onRepost: () -> Unit,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionButton(
            icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.ChatBubbleOutline,
            count = replyCount,
            tint = XBlue.value,
            onClick = onReply
        )
        ActionButton(
            icon = Icons.Default.Repeat,
            count = repostCount,
            tint = repostColor,
            active = isReposted,
            onClick = onRepost
        )
        ActionButton(
            icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            count = likeCount,
            tint = likeColor,
            active = isLiked,
            onClick = onLike
        )
        ActionButton(
            icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            count = null,
            tint = bookmarkColor,
            onClick = onBookmark
        )
        ActionButton(
            icon = Icons.Outlined.Share,
            count = null,
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = { }
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    count: Long?,
    tint: Color,
    active: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint)
        if (count != null && count > 0) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = formatCount(count),
                style = MaterialTheme.typography.labelSmall,
                color = tint
            )
        }
    }
}

fun String.highlightMentionsAndHashtags(): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val mentionPattern = Regex("@(\\w+)")
        val hashtagPattern = Regex("#(\\w+)")
        var lastIndex = 0
        val matches = (mentionPattern.findAll(this@highlightMentionsAndHashtags) +
                hashtagPattern.findAll(this@highlightMentionsAndHashtags))
            .sortedBy { it.range.first }
        for (match in matches) {
            if (match.range.first > lastIndex) {
                append(this@highlightMentionsAndHashtags.substring(lastIndex, match.range.first))
            }
            withStyle(SpanStyle(color = XBlue.value)) {
                append(match.value)
            }
            lastIndex = match.range.last + 1
        }
        if (lastIndex < this@highlightMentionsAndHashtags.length) {
            append(this@highlightMentionsAndHashtags.substring(lastIndex))
        }
    }
}

fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
        count >= 1_000 -> "%.1fK".format(count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
fun ProfileAvatar(
    avatarUrl: String,
    name: String,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val bgColor = remember(name) {
        Color((name.hashCode() and 0xFFFFFF) or 0xFF000000.toInt())
    }
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = if (avatarUrl.isBlank()) bgColor else MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (avatarUrl.isNotBlank() && avatarUrl.startsWith("data:")) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier.size(size).clip(CircleShape)
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MediaGrid(
    mediaUrls: List<String>,
    mediaType: String?,
    onMediaClick: (List<String>, Int) -> Unit
) {
    when {
        mediaUrls.size == 1 -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onMediaClick(mediaUrls, 0) },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (mediaType == "video") {
                        Icon(
                            Icons.Default.PlayCircle, contentDescription = "Play",
                            modifier = Modifier.size(48.dp), tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    AsyncImage(
                        model = mediaUrls[0], contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        mediaUrls.size > 1 -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                mediaUrls.chunked(2).forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEachIndexed { colIndex, url ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onMediaClick(mediaUrls, rowIndex * 2 + colIndex) },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize())
                            }
                        }
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}