# SWR for Compose Android

[![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr.compose/swr-android.svg)](https://central.sonatype.dev/namespace/com.kazakago.swr.compose)
[![javadoc](https://javadoc.io/badge2/com.kazakago.swr.compose/swr-android/javadoc.svg)](https://javadoc.io/doc/com.kazakago.swr.compose/swr-android)
[![Test](https://github.com/KazaKago/swr-compose/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/KazaKago/swr-compose/actions/workflows/test.yml)
[![License](https://img.shields.io/github/license/kazakago/swr-compose.svg)](LICENSE)

This is a clone library of [React SWR](https://swr.vercel.app) ported for [Jetpack Compose](https://developer.android.com/jetpack/compose).  
Currently works only on Android.  

The API specification of `React SWR` is followed as much as possible.  
Options are also supported for the most part.  

## What's "SWR"？

According to `React SWR`, "SWR" refers to  

> The name “SWR” is derived from stale-while-revalidate, a HTTP cache invalidation strategy popularized by [HTTP RFC 5861](https://www.rfc-editor.org/rfc/rfc5861). SWR is a strategy to first return the data from cache (stale), then send the fetch request (revalidate), and finally come with the up-to-date data.

## Requirements

Min SDK 23+

## Install

Add the following gradle dependency exchanging `*.*.*` for the latest release. [![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.swr.compose/swr-android.svg)](https://search.maven.org/artifact/com.kazakago.swr.compose/swr-android)  

```kotlin
implementation("com.kazakago.swr.compose:swr-android:*.*.*")
```

## Getting Started

As with `React SWR`, implement the "fetcher function" and set it with the key to `useSWR()`.  
Using Kotlin's [Destructuring declarations](https://kotlinlang.org/docs/destructuring-declarations.html) for return values can be written in the same way as in `React SWR`.  

```kotlin
private val fetcher: suspend (key: String) -> String = {
    getNameApi.execute(key)
}

@Composable
fun Profile() {
    val (data, error) = useSWR("/api/user", fetcher)

    if (error != null) {
        Text("failed to load")
        return
    }
    if (data == null) {
        Text("loading...")
        return
    }
    Text("hello $data!")
}
```

## Example

Refer to the [**example module**](example) for details. This module works as an Android app.  

## Supported Features

| Feature name                                                                  | Status                          | Note                           |
|-------------------------------------------------------------------------------|---------------------------------|--------------------------------|
| [Options](https://swr.vercel.app/docs/options)                                | [See below](#supported-options) |                                |
| [Global Configuration](https://swr.vercel.app/docs/global-configuration)      | ✅                               |                                |
| [Data Fetching](https://swr.vercel.app/docs/data-fetching)                    | ✅                               |                                |
| [Error Handling](https://swr.vercel.app/docs/error-handling)                  | ✅                               |                                |
| [Auto Revalidation](https://swr.vercel.app/docs/revalidation)                 | ✅                               |                                |
| [Conditional Data Fetching](https://swr.vercel.app/docs/conditional-fetching) | ✅                               |                                |
| [Arguments](https://swr.vercel.app/docs/arguments)                            | ✅                               |                                |
| [Mutation](https://swr.vercel.app/docs/mutation)                              | ✅️                              |                                |
| [Pagination](https://swr.vercel.app/docs/pagination)                          | ✅                               |                                |
| [Prefetching Data](https://swr.vercel.app/docs/prefetching)                   | ✅️                              | Available by `useSWRPreload()` |
| [Suspense](https://swr.vercel.app/docs/suspense)                              | ❌                               |                                |
| [Middleware](https://swr.vercel.app/docs/middleware)                          | ❌                               |                                |

## Supported Options

The following options are supported for React SWR.  
https://swr.vercel.app/docs/options  

| Option name                                               | Status | Default value                                                                      |
|-----------------------------------------------------------|--------|------------------------------------------------------------------------------------|
| suspense                                                  | ❌      |                                                                                    |
| fetcher(args)                                             | ✅      |                                                                                    |
| revalidateIfStale                                         | ✅      | true                                                                               |
| revalidateOnMount                                         | ✅      | true if `fallbackData` is not set                                                  |
| revalidateOnFocus                                         | ✅      | true                                                                               |
| revalidateOnReconnect                                     | ✅      | true                                                                               |
| refreshInterval                                           | ✅      | 0.seconds (disable)                                                                |
| refreshWhenHidden                                         | ✅      | false                                                                              |
| refreshWhenOffline                                        | ✅      | false                                                                              |
| shouldRetryOnError                                        | ✅      | true                                                                               |
| dedupingInterval                                          | ✅      | 2.seconds                                                                          |
| focusThrottleInterval                                     | ✅      | 5.seconds                                                                          |
| loadingTimeout                                            | ✅      | 3.seconds                                                                          |
| errorRetryInterval                                        | ✅      | 5.seconds                                                                          |
| errorRetryCount                                           | ✅      |                                                                                    |
| fallback                                                  | ✅      |                                                                                    |
| fallbackData                                              | ✅      |                                                                                    |
| keepPreviousData                                          | ✅      | false                                                                              |
| onLoadingSlow(key, config)                                | ✅      |                                                                                    |
| onSuccess(data, key, config)                              | ✅      |                                                                                    |
| onError(err, key, config)                                 | ✅      |                                                                                    |
| onErrorRetry(err, key, config, revalidate, revalidateOps) | ✅      | [Exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff) algorithm |
| compare(a, b)                                             | ❌      |                                                                                    |
| isPaused()                                                | ✅      | false                                                                              |
| use                                                       | ❌      |                                                                                    |

## Notice for performance

[Deduplication](https://swr.vercel.app/docs/advanced/performance#deduplication) and [Deep Comparison](https://swr.vercel.app/docs/advanced/performance#deep-comparison) are supported, but [Dependency Collection](https://swr.vercel.app/docs/advanced/performance#dependency-collection) is not supported due to Kotlin's language specification.  
Therefore, the number of re-rendering (re-compose) may be higher than in the original `React SWR`.  

However, it is possible to prevent performance degradation by limiting the number of arguments passed to child Composable functions (e.g., only data).  
