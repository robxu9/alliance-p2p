Read through the comments on [issues with the beta](http://code.google.com/p/alliancep2pbeta/issues/list) (1.2.0-pre build 1280). Some feature requests and bugfixes were already under development.

Also look through the source code for "ToDo" indicating various bugs and unfinished features.

If you're working on a feature, claim it here so we don't duplicate work. Once you're done, cross the item out. After we release a version with that feature included, the item can just be deleted.

# Feature requests #

Things on this list are pretty major features, so no need to add them all in one update.

  * Implement UDP hole punching to transverse NAT to NAT networks.
  * Replace the chat system with the [#sbu IRC channel](http://www.reddit.com/r/SBU/comments/jb9jx/there_is_a_stony_brook_irc_channel_if_anyone_is/) using [PircBot](http://www.jibble.org/pircbot.php) , [IRClib](http://moepii.sourceforge.net/), or [Martyr](http://martyr.sourceforge.net/).
    * In the meantime, implement multi-person PMs/private chatrooms.
  * Allow admins to appoint moderators who get some admin powers.
  * Let clients with different encryption levels communicate (as per [this issue](http://code.google.com/p/alliancep2pbeta/issues/detail?id=6)), which would also let us easily transition to the "experimental" AES option.
  * Monitor selected users for new files.
    * There's a [plugin](http://code.google.com/p/alliancep2pbeta/issues/detail?id=36) being developed for that.

# Minor features #

These are things that could probably be implemented quickly.

  * Change text for highlighted @usernames to black or white, depending on the background color.
  * Make rearranging the tabs take more initial movement.
  * Assign aliases to shared folders like DC++.
  * Notify users when tabs contain unread private messages, perhaps by blinking the taskbar icon or changing the window title.
  * If the user scrolls the chat window up to read old entries, don't scroll it back down when a new message appears
    * http://stackoverflow.com/questions/2039373/maintaing-jtextarea-scroll-position
    * http://www.jguru.com/faq/view.jsp?EID=16674
  * Use RPC's to send commands rather than messages.

# Bugs #

Note that some [old ones](http://code.google.com/p/alliancep2pbeta/issues/list) might still not be fixed.

  * Don't highlight an @ sign without text after it.
  * The `/whois` command should be case-insensitive.
  * Attempting to hash "会意.txt" (and probably other Unicode files) freezes Alliance.
  * People don't connect to everyone in the network, at least not quickly.
  * BufferUnderflowException when someone connects to you.
  * NullPointerException when you try to connect to your own connection code.
  * Chat history file never gets limited, and large ones can freeze Alliance.
  * The automatic database compacting hangs the UI.
  * Some files marked as "external" still get unhashed.
  * Frequent ConcurrentModificationExceptions from somewhere.
  * NullPointerException when sorting Downloads tab columns.