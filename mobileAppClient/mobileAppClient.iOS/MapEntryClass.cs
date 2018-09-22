﻿using System;
using CoreGraphics;
using CustomRenderer.iOS;
using MapKit;
using mobileAppClient.Maps;
using UIKit;
using Xamarin.Forms;

namespace mobileAppClient.iOS
{
    public class MapEntryClass
    {
        public MapEntryClass()
        {
        }

        public void addSlideUpSheet(CustomPin pin, CustomMap map, MKMapView nativeMap, CustomMapRenderer customMapRenderer) {
            //Get the current UI Window
            var window = UIApplication.SharedApplication.KeyWindow;
            var bottomSheetVC = new BottomSheetViewController(pin, map, nativeMap, customMapRenderer);

            var rootVC = window.RootViewController;

            if(rootVC.View.Subviews.Length > 1) {
                UIView subView = rootVC.View.Subviews[1];
                subView.RemoveFromSuperview();
            }

            //bottomSheetVC.ModalPresentationStyle = UIModalPresentationStyle.OverCurrentContext;
            //rootVC.AddChildViewController(bottomSheetVC);
            UIApplication.SharedApplication.Delegate.GetWindow().RootViewController = bottomSheetVC;
            //rootVC.PresentModalViewController(bottomSheetVC, true);

            //rootVC.View.AddSubview(bottomSheetVC.View);
            ////rootVC.View.BringSubviewToFront(bottomSheetVC.View);
            //bottomSheetVC.DidMoveToParentViewController(rootVC);

            //var height = window.Frame.Height;
            //var width = window.Frame.Width;
            //bottomSheetVC.View.Frame = new CGRect(0, window.Frame.GetMaxY(), width, height);



        }



    }
}