"""
Simplified GPU Test
Tests if TensorFlow can detect GPU without complex operations
"""

print("="*60)
print("GPU Detection Test")
print("="*60)

try:
    import tensorflow as tf
    print(f"\n✓ TensorFlow version: {tf.__version__}")
    
    # List physical devices
    gpus = tf.config.list_physical_devices('GPU')
    cpus = tf.config.list_physical_devices('CPU')
    
    print(f"\nCPUs detected: {len(cpus)}")
    print(f"GPUs detected: {len(gpus)}")
    
    if gpus:
        print("\n✓ GPU FOUND!")
        for i, gpu in enumerate(gpus):
            print(f"  GPU {i}: {gpu.name}")
        
        # Try to enable memory growth
        try:
            for gpu in gpus:
                tf.config.experimental.set_memory_growth(gpu, True)
            print("\n✓ GPU memory growth enabled")
        except Exception as e:
            print(f"\n⚠ Could not set memory growth: {e}")
        
        print("\n" + "="*60)
        print("GPU is available for training!")
        print("="*60)
    else:
        print("\n⚠ No GPU detected")
        print("\nPossible reasons:")
        print("1. cuDNN not installed or incompatible version")
        print("2. CUDA not properly configured")
        print("3. GPU drivers need update")
        print("\nTensorFlow will use CPU (slower training)")
        
except Exception as e:
    print(f"\n✗ Error: {e}")
    print("\nTrying to get more details...")
    
    try:
        import tensorflow as tf
        print(f"TensorFlow built with CUDA: {tf.test.is_built_with_cuda()}")
    except:
        pass

print("\n")
