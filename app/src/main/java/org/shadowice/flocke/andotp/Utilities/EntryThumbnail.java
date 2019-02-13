/*
 * Copyright (C) 2017-2018 Jakob Nixdorf
 * Copyright (C) 2017-2018 Richy HBM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatDelegate;

import org.shadowice.flocke.andotp.R;

public class EntryThumbnail {
    private enum AssetType {
        Bitmap,
        Vector
    }

    public enum EntryThumbnails {
        Default(R.mipmap.ic_launcher_round),
        Adobe(R.drawable.thumb_adobe),
        Amazon(R.drawable.thumb_amazon),
        AmazonWebServices(R.drawable.thumb_amazonwebservices),
        AngelList(R.drawable.thumb_angellist),
        Apple(R.drawable.thumb_apple),
        ArenaNet(R.drawable.thumb_arenanet),
        Atlassian(R.drawable.thumb_atlassian),
        AVM(R.drawable.thumb_avm),
        BattleNet(R.drawable.thumb_battlenet),
        Binance(R.drawable.thumb_binance),
        BitBucket(R.drawable.thumb_bitbucket),
        Bitcoin(R.drawable.thumb_bitcoin),
        Bitfinex(R.drawable.thumb_bitfinex),
        Bitstamp(R.mipmap.thumb_bitstamp, AssetType.Bitmap),
        Bitpanda(R.drawable.thumb_bitpanda),
        Bittrex(R.drawable.thumb_bittrex),
        Bitwala(R.drawable.thumb_bitwala),
        Bitwarden(R.drawable.thumb_bitwarden),
        CEXio(R.drawable.thumb_cexio),
        BlockchainInfo(R.drawable.thumb_blockchain_info),
        CloudDownload(R.drawable.thumb_cloud_download),
        Cloudflare(R.drawable.thumb_cloudflare),
        Coinbase(R.drawable.thumb_coinbase),
        ComputerBase(R.drawable.thumb_computerbase),
        CozyCloud(R.drawable.thumb_cozycloud),
        Degiro(R.drawable.thumb_degiro),
        DigitalOcean(R.drawable.thumb_digital_ocean),
        Discord(R.drawable.thumb_discord),
        Disroot(R.drawable.thumb_disroot),
        Dropbox(R.drawable.thumb_dropbox),
        Email(R.drawable.thumb_email),
        Evernote(R.drawable.thumb_evernote),
        Facebook(R.drawable.thumb_facebook),
        Fingerprint(R.drawable.thumb_fingerprint),
        Firefox(R.drawable.thumb_firefox),
        Flight(R.drawable.thumb_flight_takeoff),
        Gamepad(R.drawable.thumb_gamepad),
        Gandi(R.drawable.thumb_gandi),
        Git(R.drawable.thumb_git),
        Gitea(R.drawable.thumb_gitea),
        Github(R.drawable.thumb_github),
        Gitlab(R.drawable.thumb_gitlab),
        GoDaddy(R.drawable.thumb_godaddy),
        Google(R.drawable.thumb_google),
        HackerOne(R.drawable.thumb_hackerone),
        Heroku(R.drawable.thumb_heroku),
        Hetzner(R.drawable.thumb_hetzner),
        HMRC(R.drawable.thumb_hmrc),
        HomeAssistant(R.drawable.thumb_home_assistant),
        HumbleBundle(R.drawable.thumb_humblebundle),
        HurricaneElectric(R.drawable.thumb_hurricane_electric),
        Iconomi(R.drawable.thumb_iconomi),
        IFTTT(R.drawable.thumb_ifttt),
        Instagram(R.drawable.thumb_instagram),
        Itchio(R.drawable.thumb_itchio),
        Jagex(R.drawable.thumb_jagex),
        Kickstarter(R.drawable.thumb_kickstarter),
        Kraken(R.drawable.thumb_kraken),
        Kucoin(R.drawable.thumb_kucoin),
        LastPass(R.drawable.thumb_lastpass),
        Linode(R.drawable.thumb_linode),
        Liqui(R.drawable.thumb_liqui),
        LogMeIn(R.drawable.thumb_logmein),
        Mailgun(R.drawable.thumb_mailgun),
        Mapbox(R.drawable.thumb_mapbox),
        Mastodon(R.drawable.thumb_mastodon),
        Mediawiki(R.mipmap.thumb_mediawiki, AssetType.Bitmap),
        Mega(R.drawable.thumb_mega),
        Microsoft(R.drawable.thumb_microsoft),
        Migadu(R.drawable.thumb_migadu),
        Miraheze(R.drawable.thumb_miraheze),
        Mixer(R.drawable.thumb_mixer),
        NAS(R.drawable.thumb_nas),
        NextCloud(R.drawable.thumb_nextcloud),
        Nintendo(R.drawable.thumb_nintendo),
        NPM(R.drawable.thumb_npm),
        OpenVZ(R.drawable.thumb_openvz),
        Origin(R.drawable.thumb_origin),
        OVH(R.drawable.thumb_ovh),
        Patreon(R.drawable.thumb_patreon),
        PayPal(R.drawable.thumb_paypal),
        phpMyAdmin(R.drawable.thumb_phpmyadmin),
        Plurk(R.drawable.thumb_plurk),
        ProtonMail(R.drawable.thumb_protonmail),
        Rackspace(R.drawable.thumb_rackspace),
        Reddit(R.drawable.thumb_reddit),
        Rockstar(R.drawable.thumb_rockstar),
        RSS(R.drawable.thumb_rss),
        SAP(R.drawable.thumb_sap),
        Scaleway(R.drawable.thumb_scaleway),
        Sciebo(R.drawable.thumb_sciebo),
        Seafile(R.mipmap.thumb_seafile, AssetType.Bitmap),
        School(R.drawable.thumb_school),
        Skrill(R.drawable.thumb_skrill),
        Slack(R.drawable.thumb_slack),
        Snapchat(R.drawable.thumb_snapchat),
        Steam(R.drawable.thumb_steam),
        Stripe(R.drawable.thumb_stripe),
        Sync(R.drawable.thumb_sync),
        Synology(R.drawable.thumb_synology),
        TeaHub(R.drawable.thumb_teahub),
        TeamViewer(R.drawable.thumb_teamviewer),
        Terminal(R.drawable.thumb_terminal),
        Trello(R.drawable.thumb_trello),
        Tumblr(R.drawable.thumb_tumblr),
        Tutanota(R.drawable.thumb_tutanota),
        Twitch(R.drawable.thumb_twitch),
        Twitter(R.drawable.thumb_twitter),
        Ubisoft(R.drawable.thumb_ubisoft),
        UbuntuOne(R.drawable.thumb_ubuntu_one),
        VK(R.drawable.thumb_vk),
        Vultr(R.drawable.thumb_vultr),
        Wallabag(R.drawable.thumb_wallabag),
        Wallet(R.drawable.thumb_wallet),
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
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        if (thumbnail == EntryThumbnails.Default && size > 0) {
            LetterBitmap letterBitmap = new LetterBitmap(context);
            return letterBitmap.getLetterTile(label, label, size, size);
        } else if (thumbnail != EntryThumbnails.Default) {

            try {
                if (thumbnail.getAssetType() == AssetType.Vector) {
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
        }

        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);
    }
}
