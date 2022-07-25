import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_play_licensing/flutter_play_licensing.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _isAllowed = false;

  @override
  void initState() {
    super.initState();
    isAllowed();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> isAllowed() async {
    bool isAllowed;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      isAllowed = await FlutterPlayLicensing.isAllowed();
    } on PlatformException {
      isAllowed = false;
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _isAllowed = isAllowed;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Is allowed: $_isAllowed\n'),
        ),
      ),
    );
  }
}
