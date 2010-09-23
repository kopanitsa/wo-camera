LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := CameraNativeUtil
LOCAL_SRC_FILES := CameraNativeUtil.c
LOCAL_ARM_MODE  := arm

include $(BUILD_SHARED_LIBRARY)