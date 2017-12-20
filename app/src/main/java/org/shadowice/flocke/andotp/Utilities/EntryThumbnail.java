package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import org.shadowice.flocke.andotp.R;

public class EntryThumbnail {
    private enum AssetType {
        Bitmap,
        Vector
    }

    public enum EntryThumbnails {
        Default(R.mipmap.ic_launcher_round),
        Amazon(R.drawable.thumb_amazon),
        AmazonWebServices(R.drawable.thumb_amazonwebservices),
        AngelList(R.drawable.thumb_angellist),
        Apple(R.drawable.thumb_apple),
        ArenaNet(R.drawable.thumb_arenanet),
        BattleNet(R.drawable.thumb_battlenet),
        BitBucket(R.drawable.thumb_bitbucket),
        Bitcoin(R.drawable.thumb_bitcoin),
        Bitstamp(R.mipmap.thumb_bitstamp, AssetType.Bitmap),
        Bitwarden(R.mipmap.thumb_bitwarden, AssetType.Bitmap),
        Cloudflare(R.drawable.thumb_cloudflare),
        Coinbase(R.drawable.thumb_coinbase),
        CozyCloud(R.drawable.thumb_cozycloud),
        DigitalOcean(R.drawable.thumb_digital_ocean),
        Discord(R.drawable.thumb_discord),
        Dropbox(R.drawable.thumb_dropbox),
        Facebook(R.drawable.thumb_facebook),
        Git(R.drawable.thumb_git),
        Github(R.drawable.thumb_github),
        Gitlab(R.drawable.thumb_gitlab),
        Google(R.drawable.thumb_google),
        HurricaneElectric(R.mipmap.thumb_hurricane_electric, AssetType.Bitmap),
        IFTTT(R.drawable.thumb_ifttt),
        Itchio(R.drawable.thumb_itchio),
        Kickstarter(R.drawable.thumb_kickstarter),
        LastPass(R.drawable.thumb_lastpass),
        Mailgun(R.drawable.thumb_mailgun),
        Mastodon(R.drawable.thumb_mastodon),
        Microsoft(R.drawable.thumb_microsoft),
        NextCloud(R.drawable.thumb_nextcloud),
        Origin(R.drawable.thumb_origin),
        Patreon(R.drawable.thumb_patreon),
        PayPal(R.drawable.thumb_paypal),
        ProtonMail(R.drawable.thumb_protonmail),
        RSS(R.drawable.thumb_rss),
        Skrill(R.drawable.thumb_skrill),
        Slack(R.drawable.thumb_slack),
        Steam(R.drawable.thumb_steam),
        Stripe(R.drawable.thumb_stripe),
        TeamViewer(R.drawable.thumb_teamviewer),
        Twitch(R.drawable.thumb_twitch),
        Twitter(R.drawable.thumb_twitter),
        Ubisoft(R.drawable.thumb_ubisoft),
        UbuntuOne(R.drawable.thumb_ubuntu_one),
        Wordpress(R.drawable.thumb_wordpress);

        private int resource;
        private AssetType assetType;

        EntryThumbnails(int resource) {
            this.resource = resource;
            this.assetType = AssetType.Vector;
        }

        EntryThumbnails(int resource, AssetType assetType) {
            this.resource = resource;
            this.assetType = assetType;
        }

        public int getResource() {
            return resource;
        }
        public AssetType getAssetType() {
            return assetType;
        }
    }

    public static Bitmap getThumbnailGraphic(Context context, String label, int size, EntryThumbnails thumbnail) {
        if(thumbnail == EntryThumbnails.Default) {
            LetterBitmap letterBitmap = new LetterBitmap(context);
            return letterBitmap.getLetterTile(label, label, size, size);
        }

        try {
            if(thumbnail.getAssetType() == AssetType.Vector) {
                Drawable drawable = context.getResources().getDrawable(thumbnail.getResource());
                Bitmap bitmap = Bitmap.createBitmap(drawable.getMinimumWidth(), drawable.getMinimumHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                drawable.draw(canvas);
                return bitmap;
            } else {
                return BitmapFactory.decodeResource(context.getResources(), thumbnail.getResource());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);
    }
}
