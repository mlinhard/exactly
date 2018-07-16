from setuptools import setup


def get_version():
    try:
        with open("/opt/exactly/lib/python/VERSION", "r") as f:
            return f.read()
    except Exception as e:
        print("You need to supply VERSION file")
        raise e


unified_version = get_version()
print("Using version: " + unified_version)

setup(name='exactly',
      version=unified_version,
      description='Binary exact search',
      url='http://github.com/mlinhard/exactly',
      author='Michal Linhard',
      author_email='michal@linhard.sk',
      license='Apache 2.0',
      packages=['exactly'],
      zip_safe=False,
      install_requires=[
          'docopt', 'requests'
      ])
