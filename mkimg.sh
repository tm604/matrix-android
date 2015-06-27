#!/bin/bash
set +e
cd res

cp drawable-xxxhdpi/drawer_shadow.9.png drawable-xxxhdpi/drawer_shadow_right.9.png 
perl ~/dev/gitperl/App-nined/fliph.pl drawable-xxxhdpi/drawer_shadow_right.9.png

cp drawable-xxxhdpi/text_message_bg.9.png drawable-xxxhdpi/text_message_mine_bg.9.png 
perl ~/dev/gitperl/App-nined/fliph.pl drawable-xxxhdpi/text_message_mine_bg.9.png

perl ~/dev/gitperl/App-nined/scale.pl drawable-xxxhdpi/room_count_bg.9.png
perl ~/dev/gitperl/App-nined/scale.pl drawable-xxxhdpi/text_message_bg.9.png
perl ~/dev/gitperl/App-nined/scale.pl drawable-xxxhdpi/drawer_shadow.9.png

perl ~/dev/gitperl/App-nined/scale-image.pl drawable-xxxhdpi/default_profile.png

exit 0

